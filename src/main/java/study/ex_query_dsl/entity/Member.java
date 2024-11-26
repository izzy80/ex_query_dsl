package study.ex_query_dsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter //실무에서는 쓰지 말기
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of =  {"id", "username", "age"}) //단 연관관계 필드는 집어넣지 말기
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id") //외래키 이름
    private Team team;

    //생성자
    public Member(String username) {
        this(username, 0);
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    //연관관계 편의 메소드
    /*
     * 팀이 바뀌면, 팀도 바꾸고
     * 팀에 연관된 나도 바꾼다.
     */
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
