package client.fallback;

import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import client.UserClient;
import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return null;
    }
}
