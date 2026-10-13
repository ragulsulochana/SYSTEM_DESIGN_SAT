package com.library.service;

import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.AccountStatus;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMemberById(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID '" + id + "' not found"));
    }

    public Member createMember(Member member) {
        if (memberRepository.existsById(member.getId())) {
            throw new ConflictException("Member with ID '" + member.getId() + "' already exists");
        }
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new ConflictException("Email address '" + member.getEmail() + "' is already registered to another patron");
        }
        if (member.getAccountStatus() == null) {
            member.setAccountStatus(AccountStatus.ACTIVE);
        }
        return memberRepository.save(member);
    }

    public Member updateMember(String id, Member updatedDetails) {
        Member existing = getMemberById(id);

        // Check email uniqueness if changing email
        if (!existing.getEmail().equalsIgnoreCase(updatedDetails.getEmail())) {
            if (memberRepository.existsByEmail(updatedDetails.getEmail())) {
                throw new ConflictException("Email address '" + updatedDetails.getEmail() + "' is already in use");
            }
            existing.setEmail(updatedDetails.getEmail());
        }

        existing.setName(updatedDetails.getName());
        if (updatedDetails.getAccountStatus() != null) {
            existing.setAccountStatus(updatedDetails.getAccountStatus());
        }
        if (updatedDetails.getCountry() != null) {
            existing.setCountry(updatedDetails.getCountry());
        }

        return memberRepository.save(existing);
    }

    public void deleteMember(String id) {
        Member member = getMemberById(id);
        if (member.getTotalCheckedOut() > 0) {
            throw new ConflictException("Cannot delete member with " + member.getTotalCheckedOut() + " unreturned books on loan. Return items first.");
        }
        memberRepository.delete(member);
    }
}
