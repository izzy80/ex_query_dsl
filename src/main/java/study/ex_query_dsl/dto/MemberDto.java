package study.ex_query_dsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor //기본 생성자
public class MemberDto {
    private String username;
    private int age;

//    //기본 생성자
//    public MemberDto() {
//    }

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
