package cn.edu.nuc.digitalhuman.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileDto {
    private String nickname;
    private String phone;
    private String avatar;
}
