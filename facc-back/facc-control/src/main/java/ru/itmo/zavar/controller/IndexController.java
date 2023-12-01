package ru.itmo.zavar.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.itmo.zavar.entity.SchedulerJobInfo;
import ru.itmo.zavar.service.SchedulerJobService;

@RequiredArgsConstructor
@Controller
public class IndexController {

	private final SchedulerJobService scheduleJobService;
	
	@GetMapping("/index")
	public String index(Model model){
		List<SchedulerJobInfo> jobList = scheduleJobService.getAllJobList();
		model.addAttribute("jobs", jobList);
		return "index";
	}
	
}
