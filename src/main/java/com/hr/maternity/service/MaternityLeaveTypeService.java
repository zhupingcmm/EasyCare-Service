package com.hr.maternity.service;

import com.hr.maternity.dto.MaternityLeaveTypeRequest;
import com.hr.maternity.dto.MaternityLeaveTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 产假类型服务接口
 */
public interface MaternityLeaveTypeService {

    /**
     * 创建产假类型
     * 
     * @param request 请求参数
     * @return 产假类型响应
     */
    MaternityLeaveTypeResponse createMaternityLeaveType(MaternityLeaveTypeRequest request);

    /**
     * 分页查询产假类型
     * 
     * @param pageable 分页参数
     * @return 产假类型分页数据
     */
    Page<MaternityLeaveTypeResponse> listMaternityLeaveTypes(Pageable pageable);

    /**
     * 根据ID查询产假类型
     * 
     * @param id 产假类型ID
     * @return 产假类型响应
     */
    MaternityLeaveTypeResponse getMaternityLeaveTypeById(Integer id);

    /**
     * 更新产假类型
     * 
     * @param id 产假类型ID
     * @param request 请求参数
     * @return 产假类型响应
     */
    MaternityLeaveTypeResponse updateMaternityLeaveType(Integer id, MaternityLeaveTypeRequest request);

    /**
     * 删除产假类型（逻辑删除）
     * 
     * @param id 产假类型ID
     */
    void deleteMaternityLeaveType(Integer id);

    /**
     * 查询所有启用的产假类型
     * 
     * @return 产假类型列表
     */
    List<MaternityLeaveTypeResponse> listEnabledMaternityLeaveTypes();

    /**
     * 根据是否流产假查询
     * 
     * @param isAbortion 是否是流产假
     * @return 产假类型列表
     */
    List<MaternityLeaveTypeResponse> listByIsAbortion(Boolean isAbortion);
}
