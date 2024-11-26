package study.ex_query_dsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.ex_query_dsl.dto.MemberDto;
import study.ex_query_dsl.dto.QMemberDto;
import study.ex_query_dsl.dto.UserDto;
import study.ex_query_dsl.entity.Member;
import study.ex_query_dsl.entity.QMember;
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
    @DisplayName("간단한 select")
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
    @DisplayName("tuple select")
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

    /**
     * DTO - JPQL
     */
    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new study.ex_query_dsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * DTO - QueryDsl
     */
    @Test
    public void findDtoBySetter() {
        //setter를 통해 -> 해당 dto에 setter있어야함.
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }


    @Test
    public void findDtoByField() {
        //field를 통해 직접 주입
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        //생성자를 통해 주입
        //constructor(x, x, ...)순서가 맞아야함.
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 필드에 username이 없어서 오류가 난다. 매칭이 안 돼서
     * Member - usrename
     * UserDto - name
     *
     * userDto = UserDto(name=null, age=10)
     * userDto = UserDto(name=null, age=20)
     * userDto = UserDto(name=null, age=30)
     * userDto = UserDto(name=null, age=40)
     */
    @Test
    public void findUserDto_wrong() {
        //field를 통해 직접 주입
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDto_correct() {
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * userDto = UserDto(name=member1, age=40)
     * userDto = UserDto(name=member2, age=40)
     * userDto = UserDto(name=member3, age=40)
     * userDto = UserDto(name=member4, age=40)
     */
    @Test
    public void findUserDto_subquery() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
//                        member.username.as("name"),
                        //위 대신 아래와 같이 써도 된다.
                        ExpressionUtils.as(member.username,"name"),
                        //서브쿼리
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub),"age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * Constructor과의 차이는 얘는 오류가 나면 compile 시 알려준다.
     * Constructor는 오류나면 runtimeError
     */
    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Distinct
     */
    @Test
    public void distinct() {
        List<String> result = queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();
    }





}
