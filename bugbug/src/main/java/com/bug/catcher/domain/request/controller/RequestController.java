package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/request")
public class RequestController {
    private RequestService requestService;

    public RequestController(RequestService requestService){
        this.requestService = requestService;
    }


//    @Value("${kakao.api.key}")
//    private String kakaoMapApiKey;

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

    //create request
    @PostMapping("/createRequest")
    @ResponseBody
    public List<Map<String, Object>> createRequest(@RequestBody RequestFormDto form) {
        requestService.createRequest(form);
        System.out.println("등록 성공");
        return requestService.readRequestList();
    }

    //read request(전체 게시판)
    @GetMapping("/readWholeRequests")
    @ResponseBody
    public List<Map<String, Object>> readRequestList() {
        System.out.println("조회 성공");
        return requestService.readRequestList();
    }

    //상세보기 조회수 증가 체크(선택한 게시물 보기)
    @GetMapping("/requestDetail/{id}")
    @ResponseBody
    public Request requestDetail(@PathVariable Long id) {
        return requestService.readRequestDetail(id);
    }

    //update request
    @PatchMapping("/updateRequest/{requestId}")
    @ResponseBody
    public Request updateRequestList(
            @PathVariable Long requestId, Long loginUserId,
            @RequestBody RequestFormDto form
    ) {
        requestService.updateRequest(requestId, loginUserId, form);
        return requestService.readRequestDetail(requestId);
    }

    // delete request
    @DeleteMapping("/deleteRequest/{requestId}")
    @ResponseBody
    public List<Map<String, Object>> deleteRequestList(@PathVariable Long requestId, @PathVariable Long loginUserId) {
        requestService.deleteRequest(requestId, loginUserId);
        return requestService.readRequestList();
    }
}
