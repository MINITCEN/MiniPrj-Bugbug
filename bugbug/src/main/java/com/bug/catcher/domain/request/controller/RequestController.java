package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.request.service.RequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RequestController {
    /*private RequestService requestService;

    public RequestController(RequestService requestService){
        this.requestService = requestService;
    }


    @Value("${kakao.api.key}")
    private String kakaoMapApiKey;

    //뷰 추가되면 구현하기
    //페이징 포함 게시판
//    @GetMapping("/request")
//    public String requestMain(@RequestParam(defaultValue = "0") int page, Model model) {
//        Page<Request> requestPage = requestService.findRequestPage(page);
//
//        model.addAttribute("requestList", requestPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", requestPage.getTotalPages());
//
//        return "requestList";
//    }

    // 뷰 추가되면 구현하기
    // 헌터 의뢰글 작성 폼 연결
//    @GetMapping("/request/new")
//    public String requestForm(Model model) {
//       //
//        model.addAttribute("kakaoMapKey", kakaoMapApiKey);
//        return "requestForm";
//    }

    //create request 테스트
    @PostMapping("/request/test1")
    @ResponseBody
    public String createRequestTest(@RequestBody RequestFormDto form) {
        requestService.createRequest(form);
        System.out.println("등록 성공");
        return "등록 성공";
    }

    //read request 테스트(전체 게시판)
    @GetMapping("/request/test2")
    @ResponseBody
    public List<Request> readRequestList() {
        requestService.readRequestList();
        System.out.println("조회 성공");
        return requestService.readRequestList();
    }

    //상세보기 조회수 증가 체크
    @GetMapping("/request/test2/{id}")
    @ResponseBody
    public Request requestDetailTest(@PathVariable Long id) {
        return requestService.readRequestDetail(id);
    }

    //update request 테스트
    @PostMapping("/request/test3/{requestId}")
    @ResponseBody
    public List<Request> updateRequestList(
            @PathVariable Long requestId,
            @RequestBody RequestFormDto form
    ) {
        requestService.updateRequest(requestId, form);
        return requestService.readRequestList();
    }

    // delete request 테스트
    @PostMapping("/request/test4/{requestId}")
    @ResponseBody
    public List<Request> deleteRequestList(@PathVariable Long requestId) {
        requestService.deleteRequest(requestId);
        return requestService.readRequestList();
    }*/
}
