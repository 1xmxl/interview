package client.fallback;

import com.interview.DTO.LoginRequest;
import com.interview.DTO.LoginResponse;
import client.UserClient;
import com.interview.DTO.UserDTO;
import com.interview.DTO.UsersDTO;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {


    @Override
    public UsersDTO getUsers(String username,String password) {
        return null;
    }

    @Override
    public void saveNewUsers(UserDTO usersDTO) {

    }
}
