package com.revature.p2_lfg.service.session.classes;

import com.revature.p2_lfg.presentation.models.session.*;
import com.revature.p2_lfg.repository.interfaces.LoginRepository;
import com.revature.p2_lfg.repository.interfaces.SessionDetailsRepository;
import com.revature.p2_lfg.repository.interfaces.SessionRepository;
import com.revature.p2_lfg.repository.entities.compositeKeys.GroupSessionId;
import com.revature.p2_lfg.repository.entities.session.Games;
import com.revature.p2_lfg.repository.entities.session.Session;
import com.revature.p2_lfg.repository.entities.session.SessionDetails;
import com.revature.p2_lfg.repository.entities.session.Tag;
import com.revature.p2_lfg.repository.entities.user.UserCredential;
import com.revature.p2_lfg.service.session.MaxUsersException;
import com.revature.p2_lfg.service.session.interfaces.SessionServiceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.revature.p2_lfg.utility.JWTInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.revature.p2_lfg.service.session.dto.GroupUser;

import java.util.*;

@Service("sessionService")
    public class SessionService implements SessionServiceable {

    private final Logger iLog = LoggerFactory.getLogger("iLog");
    private final Logger dLog = LoggerFactory.getLogger("dLog");

    @Autowired
    private SessionDetailsRepository sessionDetailsRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private LoginRepository loginRepository;

    public CreatedGroupSessionResponse createGroupSession(CreateGroupSessionRequest createGroup, JWTInfo parsedJWT) {
        dLog.debug("Creating group session response from group create request: " + createGroup);
        SessionDetails sessionDetails = createGroupSessionInDatabase(createGroup.getGameId() , createGroup.getMaxUsers(), createGroup.getDescription());
        iLog.info("New Group Session created and stored in database: " + sessionDetails);

        GroupSessionId sessionId = createUserSession(sessionDetails, parsedJWT, parsedJWT.getUserId(), true);
        iLog.info("Successful entry of a user to a session: " + sessionId);
        return new CreatedGroupSessionResponse(
                sessionDetails.getGroupid(),
                sessionDetails.getGame().getGameid(),
                sessionDetails.getMaxusers(),
                sessionDetails.getDescription(),
                getGroupMembersOfSession(sessionDetails.getGroupid())
        );
    }

    @Override
    public JoinGroupSessionResponse joinGroupSession(JWTInfo parsedJWT, int groupId, int gameId) throws MaxUsersException {
        dLog.debug("Joining group session waiting room Group ID: " + groupId + " JWT: " + parsedJWT);
        return enterWaitingRoom(groupId, parsedJWT);
    }

    private JoinGroupSessionResponse enterWaitingRoom(int groupId, JWTInfo parsedJWT) throws MaxUsersException {
        dLog.debug("Attempting to enter waiting room: " + groupId);
        SessionDetails sessionDetails = getSessionDetailsByGroupId(groupId);
        if (sessionDetails != null){
            iLog.info("Joining group session: " + sessionDetails);
            GroupSessionId sessionId = createUserSession(sessionDetails, parsedJWT, findByHostId(groupId),  false);
            return new JoinGroupSessionResponse(
                    sessionId,
                    sessionDetails.getGame().getGameid(),
                    sessionDetails.getGroupid()
            );
        }
       return null;
    }

    private int findByHostId(int groupId) {
        dLog.debug("Finding host Id by Group ID: " + groupId);
        return sessionRepository.findFirst1HostidByGroupsession(new SessionDetails(groupId, new Games(), 0, 0, "", new HashSet<>())).getHostid();
    }

    private List<GroupUser> getGroupMembersOfSession(int groupId) {
        dLog.debug("Getting group users associated by group Id: " + groupId);
        List<Session> userInSession = sessionRepository.findAllByGroupId(groupId);
        List<GroupUser> groupUsers = new ArrayList<>();
        userInSession.forEach(s -> {
            Optional<UserCredential> user = loginRepository.findById(s.getUserid());
            groupUsers.add(new GroupUser(user.isPresent()? user.get().getUsername() : "NOT PRESENT", s.getGroupsession().getGroupid(), s.isInsession()));
        });
        return groupUsers;
    }

    private GroupSessionId createUserSession(SessionDetails sessionDetails, JWTInfo parsedJWT, int hostId, boolean status) throws MaxUsersException {
        dLog.debug("Creating user Session for a group: " + sessionDetails);
        if(sessionDetails.getCurrentusers() < sessionDetails.getMaxusers()) {
            sessionDetails.setCurrentusers(sessionDetails.getCurrentusers() + 1);
            sessionRepository.save(
                    new Session(
                            parsedJWT.getUserId(), hostId, sessionDetails, status
                    )
            );
            return new GroupSessionId(parsedJWT.getUserId(), hostId);
        }
        else throw new MaxUsersException();
    }

    private SessionDetails getSessionDetailsByGroupId(int groupId) {
        dLog.debug("Getting session details by group ID: " + groupId);
        Optional<SessionDetails> session = sessionDetailsRepository.findById(groupId);
        return session.orElse(null);
    }

    private SessionDetails createGroupSessionInDatabase(int gameId, int maxUsers, String description) {
        dLog.debug("Creating a group session: GameId - " + gameId + " maxUsers - " + maxUsers + " Description: " + description);
        Set<Tag> tags = new HashSet<>();
        Games shellGame = new Games(gameId, 0, "", "");
       return sessionDetailsRepository.save(
                new SessionDetails(
                        0,
                        shellGame,
                        maxUsers,
                        1,
                        description,
                        tags));
    }

    @Override
    public CheckWaitingRoomResponse checkSessionStatus(JWTInfo parsedJWT, int groupId) {
        dLog.debug("Checking session status: " + groupId + " + userId: " + parsedJWT.getUserId());
        return createWaitingRoomResponse(getSessionForUser(parsedJWT, groupId));
    }

    private CheckWaitingRoomResponse createWaitingRoomResponse(Session session) {
        return new CheckWaitingRoomResponse(
                session.isInsession(),
                session.getGroupsession().getGame().getGameid(),
                session.getGroupsession().getGroupid()
        );
    }

    private Session getSessionForUser(JWTInfo parsedJWT, int groupId) {
        return sessionRepository.findByUserIdAndGroupId(parsedJWT.getUserId(), groupId);
    }

    @Override
    public WaitingRoomResponse respondToUserSession(JWTInfo parsedJWT, WaitingRoomRequest roomRequest) {
        dLog.debug("Responding to user in session: " + roomRequest);
        int userRespondingId = loginRepository.findByUsername(roomRequest.getWaitingUsername()).getUserid();
        Session session = sessionRepository.findByUserIdAndGroupId(userRespondingId, roomRequest.getGroupId());
        if(roomRequest.isSuccess()) {
            session.setInsession(true);
            sessionRepository.save(session);
            return new WaitingRoomResponse(
                    true,
                    new GroupUser(roomRequest.getWaitingUsername(), roomRequest.getGroupId(), true )
            );
        }else{
            sessionRepository.delete(session);
            return new WaitingRoomResponse(
              false, new GroupUser()
            );
        }
    }

    @Override
    public CancelGroupResponse cancelSession(JWTInfo parsedJWT, CancelGroupRequest cancelGroup) {
        dLog.debug("Canceling group session: " + cancelGroup);
        try{
            sessionRepository.deleteAllByGroupId(cancelGroup.getGroupId());
            sessionDetailsRepository.delete(new SessionDetails(cancelGroup.getGroupId(), new Games(), 0, 0, "", new HashSet<>()));
            return new CancelGroupResponse(true);
        }catch(Exception e){
            dLog.debug(e.getMessage(), e);
            return new CancelGroupResponse(false);
        }
    }

    @Override
    public LeaveGroupResponse leaveSession(JWTInfo parsedJWT, int groupId, int gameId) {
        dLog.debug("Attempting to leave Group Session: " + groupId);
        try{
            sessionRepository.deleteByUserIdAndGroupId(parsedJWT.getUserId(), groupId);
            return new LeaveGroupResponse(true);
        }catch(Exception e){
            dLog.error(e.getMessage(), e);
            return new LeaveGroupResponse(false);
        }
    }

    @Override
    public GroupSessionResponse getGroupSession(int groupId, int gameId, JWTInfo parsedJWT) {
        dLog.debug("Attempting to get Group Session: " + groupId);
        return new GroupSessionResponse(
                groupId,
                gameId,
                getGroupMembersOfSession(groupId)
        );
    }
}