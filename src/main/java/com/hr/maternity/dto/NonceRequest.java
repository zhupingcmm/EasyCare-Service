package com.hr.maternity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nonce请求DTO")
public class NonceRequest {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 256, message = "用户ID长度不能超过256个字符")
    @Schema(description = "用户ID", example = "A5132253", required = true)
    private String username;
}
