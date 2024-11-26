package study.ex_query_dsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.ex_query_dsl.entity.Member;
import study.ex_query_dsl.entity.Team;

import static study.ex_query_dsl.entity.QMember.*;
import static study.ex_query_dsl.entity.QTeam.team;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslMiddleTest {
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

    /**
     * 프로젝션 : select 대상 지정
     * 예를 들어 select(member.username)이면 username이 String이고 하나만 선택했기때문에
     * List<String>으로 받을 수 있음
     */
    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 여러 형태의 타입을 한 번에 가져옴
     */
    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }





}
