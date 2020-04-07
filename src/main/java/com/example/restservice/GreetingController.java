package com.example.restservice;

import sepses.ondemand_extractor.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nflabs.grok.GrokException;

@RestController
public class GreetingController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	
	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
	@GetMapping("/startservice3")
	public StartService3 startservice3(@RequestParam(name = "queryString") String queryString, @RequestParam(name = "parsedQuery") String parsedQuery, @RequestParam(name = "startTime") String startTime, @RequestParam(name = "endTime") String endTime) throws IOException, ParseException, InterruptedException, URISyntaxException, org.json.simple.parser.ParseException, GrokException {
		return new StartService3	(queryString,parsedQuery,startTime,endTime);
	}
	
	
}