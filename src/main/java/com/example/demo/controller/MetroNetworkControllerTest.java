// package com.example.demo.controller;

// import com.example.demo.service.MetroNetworkService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

// @SpringBootTest
// @AutoConfigureMockMvc
// public class MetroNetworkControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private MetroNetworkService metroNetworkService; // Mock service if necessary

//     @Test
//     public void testFindRoute() throws Exception {
//         mockMvc.perform(get("/api/findRoute")
//                 .param("startStopId", "1")
//                 .param("endStopId", "2")
//                 .param("choice", "1"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string(org.hamcrest.Matchers.containsString("The shortest path")));
//     }
// }
