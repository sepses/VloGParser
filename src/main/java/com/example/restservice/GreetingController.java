package com.example.restservice;

import sepses.ondemand_extractor.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	
	
	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
	@GetMapping("/startservice")
	public StartService startservice(@RequestParam(name = "queryString") String queryString, @RequestParam(name = "parsedQuery") String parsedQuery, @RequestParam(name = "startTime") String startTime, @RequestParam(name = "endTime") String endTime) throws Exception {
		return new StartService	(queryString,parsedQuery,startTime,endTime);
	}
	
	
}