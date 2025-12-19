package com.back.global.initData

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.genFile.entity.PostGenFile
import com.back.domain.post.post.service.PostService
import com.back.global.app.AppConfig.Companion.getGenFileDirPath
import com.back.global.app.AppConfig.Companion.isTest
import com.back.global.app.CustomConfigProperties
import com.back.standard.sampleResource.SampleResource
import com.back.standard.util.Ut.file.rm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import java.util.stream.IntStream

@Profile("!prod")
@Configuration
class NotProdInitData(
    private val customConfigProperties: CustomConfigProperties,
    private val memberService: MemberService,
    private val postService: PostService
) {
    @Autowired
    @Lazy
    private lateinit var self: NotProdInitData

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            self.work1()
            self.work2()
        }
    }

    @Transactional
    fun work1() {
        if (memberService.count() > 0) return

        if (isTest()) {
            rm(getGenFileDirPath())
        }

        val memberSystem = memberService.join("system", "1234", "시스템", "")
        memberSystem.apiKey = "system"

        val memberAdmin = memberService.join("admin", "1234", "관리자", "")
        memberAdmin.apiKey = "admin"

        val memberUser1 = memberService.join("user1", "1234", "유저1", "")
        memberUser1.apiKey = "user1"

        val memberUser2 = memberService.join("user2", "1234", "유저2", "")
        memberUser2.apiKey = "user2"

        val memberUser3 = memberService.join("user3", "1234", "유저3", "")
        memberUser3.apiKey = "user3"

        val memberUser4 = memberService.join("user4", "1234", "유저4", "")
        memberUser4.apiKey = "user4"

        val memberUser5 = memberService.join("user5", "1234", "유저5", "")
        memberUser5.apiKey = "user5"

        val memberUser6 = memberService.join("user6", "1234", "유저6", "")
        memberUser6.apiKey = "user6"

        for (notProdMember in customConfigProperties.notProdMembers) {
            val member = memberService.join(
                notProdMember.username,
                "",
                notProdMember.nickname,
                notProdMember.profileImgUrl
            )

            member.apiKey = notProdMember.apiKey()
        }
    }

    @Transactional
    fun work2() {
        if (postService!!.count() > 0) return

        val memberUser1 = memberService!!.findByUsername("user1").get()
        val memberUser2 = memberService.findByUsername("user2").get()
        val memberUser3 = memberService.findByUsername("user3").get()
        val memberUser4 = memberService.findByUsername("user4").get()
        val memberUser5 = memberService.findByUsername("user5").get()
        val memberUser6 = memberService.findByUsername("user6").get()

        val post1 = postService.write(
            memberUser1,
            "축구 하실 분?",
            "14시 까지 22명을 모아야 합니다.",
            true,
            true
        )
        post1.addComment(memberUser2, "저요!")
        post1.addComment(memberUser3, "저도 할래요.")

        val post2 = postService.write(
            memberUser1,
            "배구 하실 분?",
            "15시 까지 12명을 모아야 합니다.",
            true,
            true
        )
        post2.addComment(memberUser4, "저요!, 저 배구 잘합니다.")

        val post3 = postService.write(
            memberUser2,
            "농구 하실 분?",
            "16시 까지 10명을 모아야 합니다.",
            true,
            true
        )

        val post4 = postService.write(
            memberUser3,
            "발야구 하실 분?",
            "17시 까지 14명을 모아야 합니다.",
            true,
            true
        )

        val post5 = postService.write(
            memberUser4,
            "피구 하실 분?",
            "18시 까지 18명을 모아야 합니다.",
            true,
            true
        )

        val post6 = postService.write(
            memberUser4,
            "발야구를 밤에 하실 분?",
            "22시 까지 18명을 모아야 합니다.",
            false,
            false
        )

        val post7 = postService.write(
            memberUser4,
            "발야구를 새벽 1시에 하실 분?",
            "새벽 1시 까지 17명을 모아야 합니다.",
            true,
            false
        )

        val post8 = postService.write(
            memberUser4,
            "발야구를 새벽 3시에 하실 분?",
            "새벽 3시 까지 19명을 모아야 합니다.",
            false,
            true
        )

        val post9 = postService.write(
            memberUser4,
            "테이블테니스를 하실 분있나요?",
            "테이블테니스 강력 추천합니다.",
            true,
            true
        )

        val genFile1FilePath = SampleResource.IMG_GIF_SAMPLE1.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile1FilePath)

        var genFile2FilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile2FilePath)
        post9.deleteGenFile(PostGenFile.TypeCode.attachment, 2)

        genFile2FilePath = SampleResource.IMG_JPG_SAMPLE2.makeCopy()
        post9.putGenFile(PostGenFile.TypeCode.attachment, 3, genFile2FilePath)

        val genFile3FilePath = SampleResource.IMG_JPG_SAMPLE3.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.thumbnail, genFile3FilePath)

        val newGenFile3FilePath = SampleResource.IMG_JPG_SAMPLE4.makeCopy()
        val postGenFile3 = post9.modifyGenFile(PostGenFile.TypeCode.thumbnail, 1, newGenFile3FilePath)

        post9.thumbnailGenFile = postGenFile3

        val post10 = postService.write(
            memberUser4,
            "테니스 하실 분있나요?",
            "테니스 강력 추천합니다.",
            true,
            true
        )

        val genFile4FilePath = SampleResource.IMG_WEBP_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile4FilePath)

        val genFile5FilePath = SampleResource.AUDIO_M4A_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile5FilePath)

        val genFile6FilePath = SampleResource.AUDIO_MP3_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile6FilePath)

        val genFile7FilePath = SampleResource.AUDIO_MP3_SAMPLE2.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile7FilePath)

        val genFile8FilePath = SampleResource.VIDEO_MOV_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile8FilePath)

        val genFile9FilePath = SampleResource.VIDEO_MP4_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile9FilePath)

        val genFile10FilePath = SampleResource.VIDEO_MP4_SAMPLE2.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile10FilePath)

        val post11 = postService.write(
            memberUser4,
            "테이블테니스를 하실 분있나요?",
            """
                    # 링크
                    [PPT 1 링크, `surl:ppt/1` : `/p/11/ppt/1` 주소로 변경됨](surl:ppt/1)
                    [PPT 1 링크 with 문서번호, `surl:11/ppt/1` : `/p/11/ppt/1` 주소로 변경됨](surl:11/ppt/1)
                    [PPT 1 링크 with 페이지번호, `surl:ppt/1#3` : `/p/11/ppt/1#3` 주소로 변경됨](surl:ppt/1#3)
                    [문서 링크, `surl:11` : `/p/11` 주소로 변경됨](surl:11)
                    [문서 링크 with 해시, `surl:11#링크` : `/p/11#링크` 주소로 변경됨](surl:11#링크)
                    [PPT 2 링크, `surl:ppt/2` : `/p/11/ppt/2` 주소로 변경됨](surl:ppt/2)
                    [PPT 3 링크, `surl:ppt/3` : `/p/11/ppt/3` 주소로 변경됨](surl:ppt/3)
                    
                    # PPT
                    <details ppt-id="1">
                    <summary>인간의 인생</summary>
                    <div markdown="1">
                    ---
                    marp: true
                    ---
                    
                    # 첫 번째 슬라이드
                    
                    내용
                    
                    ---
                    
                    # 두 번째 슬라이드
                    
                    - 항목 1
                    - 항목 2
                    
                    ---
                    
                    # 세 번째 슬라이드
                    
                    마지막
                    
                    </div>
                    </details>
                    
                    <details ppt-id="2">
                    <summary>토끼의 인생</summary>
                    <div markdown="1">
                    ---
                    marp: true
                    ---
                    
                    # 첫 번째 슬라이드!!
                    
                    내용
                    
                    ---
                    
                    # 두 번째 슬라이드
                    
                    - 항목 1
                    - 항목 2
                    
                    ---
                    
                    # 세 번째 슬라이드
                    
                    마지막
                    
                    </div>
                    </details>
                    
                    <details ppt-id="3">
                    <summary>DDD</summary>
                    <div markdown="1">
                    ---
                    marp: true
                    ---
                    
                    # DDD (Domain-Driven Design)
                    도메인 중심 설계 핵심 정리
                    
                    - “코드”보다 “업무(도메인)”를 먼저 이해하고 모델로 옮기는 방법
                    - 복잡한 비즈니스를 **변경에 강하게** 만들기 위한 설계 접근
                    
                    ---
                    
                    ## DDD가 필요한 순간
                    - 요구사항이 자주 바뀐다
                    - 규칙이 많고 예외가 많다
                    - “이 기능은 어디에 넣지?”가 매번 싸움 난다
                    - 이름/용어가 팀마다 다르고 문서와 코드가 따로 논다
                    
                    DDD는 “정답 패턴 모음”이라기보다  
                    **복잡성을 다루는 대화/모델링/경계 설정 방법**에 가깝다.
                    
                    ---
                    
                    ## DDD의 한 줄 정의
                    **업무 규칙(도메인)을 코드의 중심에 두고**  
                    팀의 언어(용어)와 코드 모델을 일치시키는 설계.
                    
                    - 핵심: *도메인 모델* + *유비쿼터스 언어* + *경계(BC)*
                    
                    ---
                    
                    ## 유비쿼터스 언어 (Ubiquitous Language)
                    “말하는 그대로 코드가 되는” 팀 공용 언어
                    
                    - 기획/운영/개발/QA가 같은 단어를 같은 의미로 씀
                    - 코드에서 애매한 단어 제거
                    - 예) `Data`, `Info`, `Manager`, `Handler` 남발 금지
                    - 예) `Order`, `Payment`, `Refund`, `PayoutCandidate` 처럼 **업무 용어로 명확히**
                    
                    ---
                    
                    ## 도메인 모델이란?
                    업무 규칙을 표현하는 “개념 지도”
                    
                    - 데이터 구조 + 규칙 + 상태 변화
                    - 핵심은 “CRUD”가 아니라 “규칙”이 어디에 있느냐
                    - 예) “결제 완료 후에만 배송 가능”
                    - 예) “정산은 14일 대기 후 지급 후보가 된다”
                    
                    ---
                    
                    ## 전략적 DDD vs 전술적 DDD
                    - **전략적(Strategic)**: 경계/조직/모델의 분리 (큰 그림)
                    - Bounded Context, Context Map, ACL 등
                    - **전술적(Tactical)**: 코드 구조/패턴 (구현 디테일)
                    - Entity, Value Object, Aggregate, Repository, Domain Service 등
                    
                    > 실무에서는 “전략적 DDD”가 더 큰 임팩트를 내는 경우가 많음
                    
                    ---
                    
                    ## 바운디드 컨텍스트 (Bounded Context)
                    “용어의 의미가 **고정**되는 경계”
                    
                    - 같은 단어라도 컨텍스트가 다르면 의미가 달라질 수 있음
                    - 예) `Member`
                    - 회원 컨텍스트: 가입/인증/프로필
                    - 정산 컨텍스트: 정산 대상(정산 상태/정산 정책)
                    - 경계를 나누면 좋은 점
                    - 모델이 덜 복잡해짐
                    - 팀/모듈/DB/배포 단위로 분리하기 쉬워짐
                    
                    ---
                    
                    ## 컨텍스트 맵 (Context Map)
                    컨텍스트끼리 “어떻게 연결되는지”의 지도
                    
                    - 관계 예시
                    - **Upstream / Downstream**: 상위가 모델을 쥐고 하위가 따라감
                    - **Customer/Supplier**: 고객 요구 중심으로 공급자가 맞춤
                    - **Conformist**: 그냥 상대 모델을 그대로 따름(빠르지만 종속↑)
                    - **ACL(Anti-Corruption Layer)**: 번역 계층으로 오염 방지(안전하지만 비용↑)
                    
                    ---
                    
                    ## 엔티티(Entity) vs 값 객체(Value Object)
                    - **Entity**
                    - 식별자(id)가 핵심
                    - 시간이 지나도 “같은 것”
                    - 예) Order, Member, Post
                    - **Value Object**
                    - 값(구성)이 핵심, 불변(immutable) 선호
                    - 비교는 id가 아니라 값으로
                    - 예) Money(금액+통화), Address, Period(시작/끝)
                    
                    ---
                    
                    ## 애그리게이트(Aggregate) & 루트(Aggregate Root)
                    “일관성(규칙)을 지키는 트랜잭션 경계”
                    
                    - 애그리게이트 루트만 외부에서 직접 접근/수정
                    - 규칙을 루트에서 강제
                    - 예) `Order`가 루트, `OrderLine`은 내부 구성요소
                    - 목표
                    - **불변식(invariant)**을 한 곳에서 지키기
                    
                    ---
                    
                    ## 리포지터리(Repository)
                    도메인 관점의 저장소 인터페이스
                    
                    - 도메인에서는 “컬렉션처럼” 보이게
                    - 인프라(JPA/SQL)는 어댑터에서 처리
                    
                    예)
                    - `OrderRepository.findById(orderId)`
                    - `PayoutCandidateRepository.save(candidate)`
                    
                    ---
                    
                    ## 도메인 서비스(Domain Service)
                    “어떤 한 엔티티/VO에 넣기 애매한 핵심 규칙”을 담는 곳
                    
                    - 남발하면 “Service 지옥” 되니 주의
                    - 조건
                    - 진짜로 도메인 규칙이고
                    - 특정 애그리게이트의 책임으로 보기 애매할 때
                    
                    ---
                    
                    ## 레이어링(실무에서 자주 쓰는 구조)
                    - Presentation (Controller/API)
                    - Application (UseCase/Facade)  ← 흐름/트랜잭션/오케스트레이션
                    - Domain (Entity/VO/Aggregate/Policy) ← 핵심 규칙
                    - Infrastructure (DB, MQ, 외부 API)
                    
                    핵심: **도메인이 인프라를 몰라야** 모델이 오래 감
                    
                    ---
                    
                    ## (예시) 마켓/정산 도메인 - 용어부터 잡기
                    - 주문(Order) / 결제(Payment) / 환불(Refund)
                    - 정산 후보(PayoutCandidate) / 정산 항목(PayoutItem)
                    - 수수료(Fee) / 보관(holding/escrow) / 지급(Payout)
                    
                    용어가 잡히면 코드 구조가 따라온다:
                    - 컨텍스트 분리: `order`, `payment`, `payout`
                    - 각 컨텍스트 내부에서 “같은 단어의 의미”를 고정
                    
                    ---
                    
                    ## (짧은 코드 예시) Value Object: Money
                    ```java
                    public record Money(long amount, String currency) {
                        public Money {
                            // 금액 음수 금지 같은 규칙을 값 객체가 책임짐
                            if (amount < 0) throw new IllegalArgumentException("amount < 0");
                            if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency required");
                        }
                    
                        public Money plus(Money other) {
                            // 통화가 다르면 더할 수 없다 같은 규칙도 여기서 강제
                            if (!this.currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
                            return new Money(this.amount + other.amount, this.currency);
                        }
                    }
                    ```
                    </div>
                    </details>
            """.trimIndent(),
            true,
            true
        )

        val post12 = postService.write(
            memberUser5,
            "머메이드",
            """
                    ```mermaid
                    graph TD;
                        A-->B;
                        A-->C;
                        B-->D;
                        C-->D;
                    ```
            """.trimIndent(),
            true,
            true
        )

        IntStream.rangeClosed(13, 100).forEach { i: Int ->
            postService.write(
                memberUser5,
                "테스트 게시물 $i",
                "테스트 게시물 $i 내용",
                i % 3 != 0,
                i % 4 != 0
            )
        }

        IntStream.rangeClosed(101, 200).forEach { i: Int ->
            postService.write(
                memberUser6,
                "테스트 게시물 $i",
                "테스트 게시물 $i 내용",
                i % 5 != 0,
                i % 6 != 0
            )
        }
    }
}