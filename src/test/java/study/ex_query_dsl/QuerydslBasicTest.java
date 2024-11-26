package study.ex_query_dsl;

import com.querydsl.core.QueryResults;
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

import java.util.List;

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

    /**
     * 1. fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
     * 2. fetchOne() : 단 건 조회
     * - 조회 결과가 없으면 null
     * - 결과가 2개 이상이면 NonUniqueResultException
     * 3. fetchFirst() : limit(1).fetchOne()
     * 4. fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
     * 5. fetchCount() : count 쿼리로 변경해서 count 수 조회
     */
    @Test
    public void resultFetch() {
//        //List
//        List<Member> findMember = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        //단 건
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        //처음 한 건 조회
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();

        //페이징에서 사용
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();

        //count 쿼리로 변경
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100)) //100살인 사람 찾아
                .orderBy(member.age.desc(), member.username.asc().nullsLast()) //나이 내림차순, 이름 오름차순
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }




}
