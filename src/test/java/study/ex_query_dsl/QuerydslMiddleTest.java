package study.ex_query_dsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import static org.assertj.core.api.Assertions.*;

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

    /**
     *
     */
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * parameter가 null이냐 아니냐에 따라 동적으로 바뀌어야 함.
     * @param usernameCond
     * @param ageCond
     * @return
     */
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder(); //여기에 초기값을 넣을 수도 있음

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder) //and,or 조립 가능
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameCond), ageEq(ageCond)) //null이 들어가면 무시 됨
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
//        if(usernameCond != null){
//            return null;
//        }
        //간단할 때는 삼항연산자 사용
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    //광고상태가 isValid, 날짜가 IN: isServiceable ... 조건이 여러 개여야 할 수 있다.
    //이걸 함수로 빼고 다른 쿼리에서 이용할 수도 있다.

    //조립이 가능하다는 장점
    private Predicate allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }





}
