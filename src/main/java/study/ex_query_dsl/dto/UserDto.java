package study.ex_query_dsl.dto;

import lombok.Data;

@Data
public class UserDto {
    private String name;
    private int age;

    public UserDto() {
    }

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
