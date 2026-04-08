package com.example.demo.Playwright;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

//    private final PCTerminalService pcTerminalService;
    private final SalesPortalService salesPortalService;

    public TestController(SalesPortalService salesPortalService) {
        this.salesPortalService = salesPortalService;
    }

//    public TestController(PCTerminalService pcTerminalService, SalesPortalService salesPortalService) {
//        this.pcTerminalService = pcTerminalService;
//        this.salesPortalService = salesPortalService;
//    }

//    @PostMapping("/pct")
//    public ResponseEntity<String> runPctTest(@RequestBody TestDTO request) {
//        try {
//            String result = pcTerminalService.runPctTest(request);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(e.getMessage());
//        }
//    }
    @PostMapping("/sp")
    public ResponseEntity<String> runSalesPortalTest(@RequestBody TestDTO request) {
        try {
            String result = salesPortalService.runSalesPortalTest(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

}
