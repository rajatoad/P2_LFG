package com.revature.p2_lfg.repository.interfaces;

import com.revature.p2_lfg.repository.entities.compositeKeys.GroupSessionId;
import com.revature.p2_lfg.repository.entities.session.Session;
import com.revature.p2_lfg.repository.entities.session.SessionDetails;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, GroupSessionId> {

    int findFirst1HostidByGroupsession(SessionDetails groupsession);

    @Query("select s from Session s where s.groupsession.groupid = ?1")
    List<Session> findAllByGroupId(int groupid);

    @Override
    <S extends Session> S save(S entity);

    @Query("select s from Session s where s.userid = ?1 and s.groupsession.groupid = ?2")
    Session findByUserIdAndGroupId(int userId, int groupid);

    @Modifying
    @Query("delete from Session s where s.groupsession.groupid = ?1")
    void deleteAllByGroupId(int groupid);

    @Modifying
    @Query("delete from Session s where s.userid = ?1 and s.groupsession.groupid = ?2")
    void deleteByUserIdAndGroupId(int userId, int groupid);
}