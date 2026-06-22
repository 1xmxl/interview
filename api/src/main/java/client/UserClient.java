package client;

import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import client.fallback.UserClientFallback;

import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service",fallbackFactory = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/users/me")
    UsersDTO getUsers(@RequestParam("username") String username,@RequestParam("password") String password);
    @PostMapping("/users/save")
    void saveNewUsers(@RequestBody UserDTO usersDTO);
}
