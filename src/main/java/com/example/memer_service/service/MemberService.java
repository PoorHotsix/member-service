package com.example.memer_service.service;



import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.memer_service.domain.Address;
import com.example.memer_service.domain.Member;
import com.example.memer_service.dto.MemberDto;
import com.example.memer_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;


    //회원 등록
    @Transactional
    public Long join(MemberDto memberDto) {

        Member member = memberRepository.save(dtoToEntity(memberDto));

        return member.getId();

    }

    //회원 상세조회
    public MemberDto findMember(Long id) {
        Optional<Member> result = memberRepository.findById(id);

        Member member = result.orElseThrow(() -> new RuntimeException(id + "에 해당하는 회원이 존재하지 않습니다."));

        return entityToDto(member);
    }
    

    //회원 목록 조회
    public List<MemberDto> findMemberAll() {

        return memberRepository.findAll().stream().map(member -> entityToDto(member)).collect(Collectors.toList());
        
    }


    private Member dtoToEntity(MemberDto memberDto) {
        return Member.builder()            
            .name(memberDto.getName())
            .email(memberDto.getEmail())
            .phoneNumber(memberDto.getPhoneNumber())
            .address(new Address(memberDto.getZipcode(), memberDto.getCity(), memberDto.getStreet()))   
            .build();    
    }


    private MemberDto entityToDto(Member member) {
        return MemberDto.builder()
            .id(member.getId())
            .name(member.getName())
            .email(member.getEmail())
            .phoneNumber(member.getPhoneNumber())
            .zipcode(member.getAddress().getZipcode())
            .street(member.getAddress().getStreet())
            .city(member.getAddress().getCity())
            .build();
    }


}
