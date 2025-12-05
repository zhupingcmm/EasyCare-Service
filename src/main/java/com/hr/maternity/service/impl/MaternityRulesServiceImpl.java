package com.hr.maternity.service.impl;

import com.hr.maternity.dto.MaternityLeaveTypeResponse;
import com.hr.maternity.dto.MaternityRulesRequest;
import com.hr.maternity.dto.MaternityRulesResponse;
import com.hr.maternity.entity.CityDO;
import com.hr.maternity.entity.MaternityLeaveType;
import com.hr.maternity.entity.MaternityRules;
import com.hr.maternity.repository.CityRepository;
import com.hr.maternity.repository.MaternityLeaveTypeRepository;
import com.hr.maternity.repository.MaternityRulesRepository;
import com.hr.maternity.service.MaternityRulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 产假规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaternityRulesServiceImpl implements MaternityRulesService {

    private final MaternityRulesRepository maternityRulesRepository;
    private final MaternityLeaveTypeRepository maternityLeaveTypeRepository;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public MaternityRulesResponse createMaternityRules(MaternityRulesRequest request) {
        log.info("开始创建产假规则，请求参数: {}", request);

        // 查找城市
        CityDO city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，ID: " + request.getCityId()));

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(request.getMaternityLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + request.getMaternityLeaveTypeId()));

        MaternityRules maternityRules = new MaternityRules();
        maternityRules.setCity(city);
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setDefaultDays(request.getDefaultDays());
        maternityRules.setDoctorRecommendDays(request.getDoctorRecommendDays());
        maternityRules.setMaternityLeaveExt(request.getMaternityLeaveExt());
        maternityRules.setIsExtendable(request.getIsExtendable());
        maternityRules.setHolidayExtend(request.getHolidayExtend());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setEnabled(request.getEnabled());

        MaternityRules saved = maternityRulesRepository.save(maternityRules);
        log.info("产假规则创建成功，ID: {}", saved.getId());

        return convertToResponse(saved);
    }



    @Override
    public Page<MaternityRulesResponse> listAllMaternityRules(String city, Pageable pageable) {
        log.info("分页查询产假规则，城市: {}, page: {}, size: {}", city, pageable.getPageNumber(), pageable.getPageSize());

        Page<MaternityRules> maternityRulesPage;
        if (city != null && !city.trim().isEmpty()) {
            // 按城市代码过滤，需要先查找城市ID
            CityDO cityDO = cityRepository.findByCode(city)
                    .orElseThrow(() -> new IllegalArgumentException("城市不存在，代码: " + city));
            maternityRulesPage = maternityRulesRepository.findByCityIdAndEnabled(cityDO.getId(), true, pageable);
        } else {
            // 查询所有
            maternityRulesPage = maternityRulesRepository.findByEnabled(true, pageable);
        }
        return maternityRulesPage.map(this::convertToResponse);
    }


    @Override
    @Transactional
    public MaternityRulesResponse updateMaternityRules(Integer id, MaternityRulesRequest request) {
        log.info("更新产假规则，ID: {}, 请求参数: {}", id, request);

        MaternityRules maternityRules = maternityRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假规则不存在，ID: " + id));

        // 查找城市
        CityDO city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new IllegalArgumentException("城市不存在，ID: " + request.getCityId()));

        // 查找产假类型
        MaternityLeaveType maternityLeaveType = maternityLeaveTypeRepository.findById(request.getMaternityLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("产假类型不存在，ID: " + request.getMaternityLeaveTypeId()));

        maternityRules.setCity(city);
        maternityRules.setMaternityLeaveType(maternityLeaveType);
        maternityRules.setDefaultDays(request.getDefaultDays());
        maternityRules.setDoctorRecommendDays(request.getDoctorRecommendDays());
        maternityRules.setMaternityLeaveExt(request.getMaternityLeaveExt());
        maternityRules.setIsExtendable(request.getIsExtendable());
        maternityRules.setHolidayExtend(request.getHolidayExtend());
        maternityRules.setHasAllowance(request.getHasAllowance());
        maternityRules.setEnabled(request.getEnabled());

        MaternityRules updated = maternityRulesRepository.save(maternityRules);
        log.info("产假规则更新成功，ID: {}", updated.getId());

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMaternityRules(Integer id) {
        log.info("逻辑删除产假规则，ID: {}", id);

        MaternityRules maternityRules = maternityRulesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产假规则不存在，ID: " + id));

        maternityRules.setEnabled(false);
        maternityRulesRepository.save(maternityRules);
        log.info("产假规则逻辑删除成功，ID: {}", id);
    }

    @Override
    @Transactional
    public int batchImportMaternityRules(List<Map<String, Object>> dataList) {
        return 1;
    }

    /**
     * 转换为响应DTO
     */
    private MaternityRulesResponse convertToResponse(MaternityRules maternityRules) {
        CityDO city = maternityRules.getCity();
        return MaternityRulesResponse.builder()
                .id(maternityRules.getId())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getChineseName() : null)
                .maternityLeaveType(convertLeaveTypeToResponse(maternityRules.getMaternityLeaveType()))
                .defaultDays(maternityRules.getDefaultDays())
                .doctorRecommendDays(maternityRules.getDoctorRecommendDays())
                .maternityLeaveExt(maternityRules.getMaternityLeaveExt())
                .isExtendable(maternityRules.getIsExtendable())
                .holidayExtend(maternityRules.getHolidayExtend())
                .hasAllowance(maternityRules.getHasAllowance())
                .enabled(maternityRules.getEnabled())
                .createDate(maternityRules.getCreateDate())
                .createBy(maternityRules.getCreateBy())
                .updateDate(maternityRules.getUpdateDate())
                .updateBy(maternityRules.getUpdateBy())
                .build();
    }

    /**
     * 转换产假类型为响应DTO
     */
    private MaternityLeaveTypeResponse convertLeaveTypeToResponse(MaternityLeaveType leaveType) {
        if (leaveType == null) {
            return null;
        }
        return MaternityLeaveTypeResponse.builder()
                .id(leaveType.getId())
                .code(leaveType.getCode())
                .name(leaveType.getName())
                .isAbortion(leaveType.getIsAbortion())
                .remark(leaveType.getRemark())
                .enabled(leaveType.getEnabled())
                .createDate(leaveType.getCreateDate())
                .createBy(leaveType.getCreateBy())
                .updateDate(leaveType.getUpdateDate())
                .updateBy(leaveType.getUpdateBy())
                .build();
    }
}
