package com.revature.p2_lfg.repository.DAO.implementation;

import com.revature.p2_lfg.repository.DAO.interfaces.Sessionable;
import com.revature.p2_lfg.repository.entities.Session;
import com.revature.p2_lfg.repository.entities.compositeKeys.GroupSessionId;
import org.springframework.stereotype.Repository;
import com.revature.p2_lfg.service.session.dto.GroupUser;

import java.util.List;

@Repository("sessionDao")
public class SessionDao implements Sessionable {
    @Override
    public GroupSessionId createUserSessionEntry(Session session) {
        return null;
    }

    @Override
    public List<GroupUser> getGroupMembersByGroupId(int groupId) {
        return null;
    }

    @Override
    public int getHostId(int groupId) {
        return 0;
    }

    @Override
    public Session getUserSession(int userId, int groupId) {
        return null;
    }

    @Override
    public void save(Session session) {

    }

    @Override
    public void delete(Session session) {

    }
}
