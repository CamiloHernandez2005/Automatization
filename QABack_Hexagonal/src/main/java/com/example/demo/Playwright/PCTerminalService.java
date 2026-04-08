//package com.example.AutomatizationQA.Playwright;
//
//import com.example.AutomatizationQA.Models.Region;
//import com.example.AutomatizationQA.Repositorys.RegionRepository;
//import com.microsoft.playwright.*;
//import com.microsoft.playwright.options.WaitForSelectorState;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class PCTerminalService {
//
//    private final RegionRepository regionRepository;
//
//    public PCTerminalService(RegionRepository regionRepository) {
//        this.regionRepository = regionRepository;
//    }
//
//    public String runPctTest(TestDTO request) {
//        Region region = regionRepository.findById(request.getRegionId())
//                .orElseThrow(() -> new RuntimeException("Region not found with id " + request.getRegionId()));
//
//        String url = "http://" + region.getIp() + ":" + region.getPort();
//
//        try (Playwright playwright = Playwright.create()) {
//            Browser browser = playwright.chromium()
//                    .launch(new BrowserType.LaunchOptions().setHeadless(false));
//            Page page = browser.newPage();
//
//            page.navigate(url);
//            page.fill("input[name=\"username\"]", request.getUsername());
//            page.fill("input[name=\"password\"]", request.getPassword());
//            page.click("button[type=\"submit\"]");
//
//            page.waitForSelector("text=My Dashboard");
//
//            page.click("a.icon.wb-search[role=\"button\"]");
//            page.fill("input[placeholder='Search Products']", request.getProduct());
//            page.click("#autocomplete-result-0");
//
//            Locator modal = page.locator(".modal-body:visible");
//            modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//
//            if (request.isPhoneNumberEnabled()) {
//                Locator phoneNumber = modal.locator("input[name=\"phoneNumber\"]");
//                if (phoneNumber.isVisible()) {
//                    phoneNumber.fill(request.getPhoneNumber());
//                }
//                Locator phoneNumberConfirm = modal.locator("input[name=\"phoneNumberConfirm\"]");
//                if (phoneNumberConfirm.isVisible()) {
//                    phoneNumberConfirm.fill(request.getPhoneNumber());
//                }
//            }
//
//
//            if (request.isPhoneNumberEnabled()) {
//                Locator amount = modal.locator("input[name=\"amount\"]");
//                if (amount.isVisible()) {
//                    amount.fill(request.getAmount());
//                }
//            }
//
//            if (request.isClerkIdEnabled()) {
//                Locator clerkId = modal.locator("input[name=\"clerkId\"]");
//                if (clerkId.isVisible()) {
//                    clerkId.focus();
//                    clerkId.fill(request.getClerkId());
//                }
//            }
//
//            Locator purchaseBtn = modal.locator("a", new Locator.LocatorOptions().setHasText("Purchase"));
//            purchaseBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//            purchaseBtn.click();
//
//            Locator pagePrintLocator = modal.locator(".page-print");
//            pagePrintLocator.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//
//            List<String> resultTransaction = pagePrintLocator.allInnerTexts().stream()
//                    .distinct()
//                    .toList();
//
//            Locator finish = modal.locator("a", new Locator.LocatorOptions().setHasText("Finish"));
//            finish.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
//            finish.click();
//
//            return "Transaction completed: " + resultTransaction;
//
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }
//
//}