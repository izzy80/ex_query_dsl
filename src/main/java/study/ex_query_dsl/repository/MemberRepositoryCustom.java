package study.ex_query_dsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.ex_query_dsl.dto.MemberSearchCondition;
import study.ex_query_dsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
