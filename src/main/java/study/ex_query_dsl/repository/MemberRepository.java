package study.ex_query_dsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.ex_query_dsl.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);

}
