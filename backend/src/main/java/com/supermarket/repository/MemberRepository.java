/**
 * 会员数据访问层 —— 对应 members 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 根据会员卡号查找（POS 刷卡验证时使用） */
    Optional<Member> findByCardNo(String cardNo);

    /** 根据手机号查找 */
    Optional<Member> findByPhone(String phone);

    /** 根据姓名模糊搜索（分页） */
    Page<Member> findByNameContaining(String keyword, Pageable pageable);

    /** 查询所有正常状态的会员 */
    List<Member> findByStatus(Integer status);

    /** 检查会员卡号是否已存在 */
    boolean existsByCardNo(String cardNo);

    /** 检查手机号是否已存在 */
    boolean existsByPhone(String phone);
}
