package study.ex_query_dsl.repository;

import study.ex_query_dsl.dto.MemberSearchCondition;
import study.ex_query_dsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
