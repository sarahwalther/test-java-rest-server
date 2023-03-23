package com.example.customerprofile.api;

import java.util.Optional;
import java.util.stream.Stream;

import com.example.customerprofile.domain.CustomerProfileChangeRequest;
import com.example.customerprofile.domain.CustomerProfileCreateRequest;
import com.example.customerprofile.domain.CustomerProfileResponse;
import com.example.customerprofile.domain.CustomerProfileService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerProfileController.class)
class CustomerProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerProfileService service;

    @Nested
    class Create {

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldDelegateToService() throws Exception {

            when(service.create(any()))
                    .thenReturn(new CustomerProfileResponse("profile-id", "Joe", "Doe", "joe.doe@test.org"));

            var requestBody = "{" +
                    "\"firstName\": \"Joe\"," +
                    "\"lastName\": \"Doe\"," +
                    "\"email\": \"joe.doe@test.org\"" +
                    "}";

            mockMvc.perform(post("/api/customer-profiles")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/customer-profiles/profile-id"))
                    .andExpect(content().json("{" +
                            "\"id\": \"profile-id\"," +
                            "\"firstName\": \"Joe\"," +
                            "\"lastName\": \"Doe\"," +
                            "\"email\": \"joe.doe@test.org\"" +
                            "}"));

            var profileCaptor = ArgumentCaptor.forClass(CustomerProfileCreateRequest.class);
            verify(service).create(profileCaptor.capture());

            var profile = profileCaptor.getValue();
            assertThat(profile).isNotNull();
            assertThat(profile.getFirstName()).isEqualTo("Joe");
            assertThat(profile.getLastName()).isEqualTo("Doe");
            assertThat(profile.getEmail()).isEqualTo("joe.doe@test.org");
        }

        @WithMockUser( authorities = { "message.read", "message.write" } )
        @Test
        void shouldReturnBadRequestWhenEmailIsNotProvided() throws Exception {
            var requestBody = "{" +
                    "\"firstName\": \"Joe\"," +
                    "\"lastName\": \"Doe\"" +
                    "}";

            mockMvc.perform(post("/api/customer-profiles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                            .accept(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(service);
        }
    }

    @Nested
    class Update {

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldDelegateToService() throws Exception {

            when(service.change(any(), any()))
                    .thenReturn(Optional.of(new CustomerProfileResponse("profile-id", "Joe", "Doe", "joe.doe@test.org")));

            var requestBody = "{" +
                    "\"firstName\": \"Joe\"," +
                    "\"lastName\": \"Doe\"" +
                    "}";

            mockMvc.perform(patch("/api/customer-profiles/profile-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf())
                            .accept(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{" +
                            "\"id\": \"profile-id\"," +
                            "\"firstName\": \"Joe\"," +
                            "\"lastName\": \"Doe\"," +
                            "\"email\": \"joe.doe@test.org\"" +
                            "}"));

            var profileCaptor = ArgumentCaptor.forClass(CustomerProfileChangeRequest.class);
            verify(service).change(eq("profile-id"), profileCaptor.capture());

            var profile = profileCaptor.getValue();
            assertThat(profile).isNotNull();
            assertThat(profile.getFirstName()).isEqualTo("Joe");
            assertThat(profile.getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    class Delete {

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldDelegateToService() throws Exception {

            mockMvc.perform(delete("/api/customer-profiles/profile-id")
                            .accept(MediaType.APPLICATION_JSON).with(csrf()))
                    .andExpect(status().isOk());

            verify(service).delete(eq("profile-id"));
        }
    }

    @Nested
    class Get {

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldDelegateToService() throws Exception {

            var id = "customer-profile-id";
            when(service.getById(any()))
                    .thenReturn(Optional.of(new CustomerProfileResponse(id, "Joe", "Doe", "joe.doe@test.org")));

            mockMvc.perform(get("/api/customer-profiles/" + id)
                            .accept(MediaType.APPLICATION_JSON).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{" +
                            "\"id\": \"customer-profile-id\"," +
                            "\"firstName\": \"Joe\"," +
                            "\"lastName\": \"Doe\"," +
                            "\"email\": \"joe.doe@test.org\"" +
                            "}"));

            verify(service).getById(id);
        }

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldReadAllDelegateToService() throws Exception {

            when(service.getAll())
                    .thenReturn(Stream.of(new CustomerProfileResponse("customer-profile-id", "Joe", "Doe", "joe.doe@test.org")));

            mockMvc.perform(get("/api/customer-profiles/")
                            .accept(MediaType.APPLICATION_JSON).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[{" +
                            "\"id\": \"customer-profile-id\"," +
                            "\"firstName\": \"Joe\"," +
                            "\"lastName\": \"Doe\"," +
                            "\"email\": \"joe.doe@test.org\"" +
                            "}]"));

            verify(service).getAll();
        }

        @Test
        @WithMockUser( authorities = { "message.read", "message.write" } )
        void shouldReturnNotFoundWhenNotExists() throws Exception {

            var id = "customer-profile-id";
            when(service.getById(any())).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/customer-profiles/" + id)
                            .accept(MediaType.APPLICATION_JSON).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(""));

            verify(service).getById(id);
        }
    }
}
