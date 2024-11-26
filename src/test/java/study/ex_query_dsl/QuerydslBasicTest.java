package study.ex_query_dsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.ex_query_dsl.entity.Member;
import study.ex_query_dsl.entity.QMember;
import study.ex_query_dsl.entity.Team;

import static org.assertj.core.api.Assertions.*;
import static study.ex_query_dsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory; //여기에 써도 동시성 문제 없음

    @BeforeEach //테스트 하기 전에 항상 실행해줌
    public void before() {
        queryFactory  = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

        System.out.println(findMember);
    }

//    @Test
//    public void startQuerydsl() {
////        queryFactory  = new JPAQueryFactory(em);
////        QMember m = new QMember("m"); //1. 별칭 직접 지정
//        QMember m = QMember.member; //2. 기본 인스턴스 사용
//
//        Member findMember = queryFactory
//                .select(m)
//                .from(m)
//                .where(m.username.eq("member1")) //파라미터 바인딩 자동으로 처리
//                .fetchOne();
//
//        assertThat(findMember.getUsername()).isEqualTo("member1");
//    }

    @Test
    public void startQuerydsl() {
        //3. static 이용
        //보통 이와 같은 방식을 사용하고 선언해서 쓰는 경우는 셀프 조인할 때!
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 검색 조건 쿼리
     */
    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                //이름이 member1이면서 나이가 10살인 사람을 조회해
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))) //chain을 and, or 가능
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                //chain이 and이면 ,로 쓸 수 있음
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }




}
