package study.ex_query_dsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static study.ex_query_dsl.entity.QTeam.team;

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

    /**
     * 페이징
     */
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResult = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작
                .limit(2)
                .fetchResults();

        assertThat(queryResult.getTotal()).isEqualTo(4); //총 데이터의 개수
        assertThat(queryResult.getLimit()).isEqualTo(2); //한 번에 가져올 데이터의 개수
        assertThat(queryResult.getOffset()).isEqualTo(1); //인덱스 번호
        assertThat(queryResult.getResults().size()).isEqualTo(2); //실제 반환된 결과의 크기
    }

    /**
     * 조인
     */
    @Test
    public void aggregation(){
        //데이터 타입이 여러 개라 tuple을 사용
        //그런데 사실 실무에서는 tuple을 잘 쓰지 않음
        //DTO로 직접 뽑는 방법이 있는데 이걸 더 많이 쓰임
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     *
     */
    @Test
    @DisplayName("group")
    void group() throws Exception {
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); //(10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); //(30 + 40) / 2
    }



}
