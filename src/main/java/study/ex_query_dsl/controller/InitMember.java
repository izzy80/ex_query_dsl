package study.ex_query_dsl.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.ex_query_dsl.entity.Member;
import study.ex_query_dsl.entity.Team;


@Profile("local") //yml이 local일 때 app이 실행되면 그때 같이 실행이 된다.
@Component
@RequiredArgsConstructor
public class InitMember {
    private final InitMemberService initMemberService;

    //PostConstruct와 Transactional을 같이 못 써서 분리해줌
    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }



    }
}
