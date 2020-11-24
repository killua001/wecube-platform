package com.webank.wecube.platform.core.service.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.BindedInterfaceEntityDto;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DataModelEntityDto;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto.TrimmedPluginPackageEntityDto;
import com.webank.wecube.platform.core.dto.plugin.DynamicDataModelPullResponseDto;
import com.webank.wecube.platform.core.dto.plugin.DynamicEntityAttributeDto;
import com.webank.wecube.platform.core.dto.plugin.DynamicPluginEntityDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.repository.plugin.PluginConfigsMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageDataModelMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackagesMapper;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class PluginPackageDataModelService {
    private static final Logger log = LoggerFactory.getLogger(PluginPackageDataModelService.class);
    private static final String dataModelUrl = "http://{gatewayUrl}/{packageName}/{dataModelUrl}";

    private static final String ATTRIBUTE_KEY_SEPARATOR = "`";

    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private PluginPackageAttributesMapper pluginPackageAttributesMapper;

    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntitiesMapper;

    @Autowired
    private PluginConfigsMapper pluginConfigsMapper;

    @Autowired
    private PluginPackagesMapper pluginPackagesMapper;

    @Autowired
    private PluginPackageDataModelMapper pluginPackageDataModelMapper;

    @Autowired
    private PluginPackageMgmtService pluginPackageMgmtService;

    /**
     * 
     * @param pluginPackageDataModelDto
     * @return
     */
    // public PluginPackageDataModelDto register(PluginPackageDataModelDto
    // pluginPackageDataModelDto) {
    // return register(pluginPackageDataModelDto, false);
    // }

    // TODO
    // FIXME
    // public PluginPackageDataModelDto register(PluginPackageDataModelDto
    // pluginPackageDataModelDto,
    // boolean fromDynamicUpdate) {
    // PluginPackages latestVersionPluginPackage = pluginPackageMgmtService
    // .fetchLatestVersionPluginPackage(pluginPackageDataModelDto.getPackageName());
    //
    // if (latestVersionPluginPackage == null) {
    // String msg = String.format("Cannot find the package [%s] while
    // registering data model",
    // pluginPackageDataModelDto.getPackageName());
    // log.error(msg);
    // throw new WecubeCoreException("3116", msg,
    // pluginPackageDataModelDto.getPackageName());
    // }
    //
    // if (fromDynamicUpdate) {
    // pluginPackageDataModelDto.setDynamic(true);
    // pluginPackageDataModelDto.setUpdateTime(System.currentTimeMillis());
    // pluginPackageDataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
    // }
    //
    // PluginPackageDataModel existingDataModelDomain =
    // pluginPackageDataModelMapper
    // .selectLatestDataModelByPackageName(pluginPackageDataModelDto.getPackageName());
    //
    // int newDataModelVersion = 1;
    // if (existingDataModelDomain != null) {
    // newDataModelVersion = existingDataModelDomain.getVersion() + 1;
    // }
    // pluginPackageDataModelDto.setVersion(newDataModelVersion);
    // PluginPackageDataModel pluginPackageDataModel =
    // PluginPackageDataModelDto.toDomain(pluginPackageDataModelDto);
    //
    // if (pluginPackageDataModelDto.getPluginPackageEntities() != null
    // && !pluginPackageDataModelDto.getPluginPackageEntities().isEmpty()) {
    // Map<String, String> attributeReferenceNameMap =
    // buildAttributeReferenceNameMap(pluginPackageDataModelDto);
    // Map<String, PluginPackageAttributes> referenceAttributeMap =
    // buildReferenceAttributeMap(
    // pluginPackageDataModel);
    // updateAttributeReference(pluginPackageDataModel,
    // attributeReferenceNameMap, referenceAttributeMap);
    // }
    //
    // return
    // convertDataModelDomainToDto(pluginPackageDataModelMapper.save(pluginPackageDataModel));
    // }

    /**
     * Plugin model overview
     *
     * @return an list of data model DTOs consist of entity dtos which contain
     *         both entities and attributes
     */
    public Set<PluginPackageDataModelDto> overview() {
        Set<PluginPackageDataModelDto> pluginPackageDataModelDtos = new HashSet<>();
        List<PluginPackages> pluginPackagesEntities = pluginPackagesMapper.selectAllDistinctPackages();

        if (pluginPackagesEntities == null) {
            return pluginPackageDataModelDtos;
        }

        for (PluginPackages pluginPackagesEntity : pluginPackagesEntities) {

            PluginPackageDataModel dataModelEntity = pluginPackageDataModelMapper
                    .selectLatestDataModelByPackageName(pluginPackagesEntity.getName());
            if (dataModelEntity != null) {
                PluginPackageDataModelDto dto = buildPackageViewPluginPackageDataModelDto(dataModelEntity);
                pluginPackageDataModelDtos.add(dto);
            }
        }

        return pluginPackageDataModelDtos;
    }

    /**
     * View one data model entity with its relationship by packageName
     *
     * @param packageName
     *            the name of package
     * @return list of entity dto
     */
    public PluginPackageDataModelDto packageView(String packageName) {
        PluginPackages latestPluginPackage = pluginPackageMgmtService.fetchLatestVersionPluginPackage(packageName);

        if (latestPluginPackage == null) {
            String msg = String.format("Plugin package with name [%s] is not found", packageName);
            log.info(msg);
            return null;
        }

        PluginPackageDataModel latestDataModelEntity = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(packageName);

        if (latestDataModelEntity == null) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            log.error(errorMessage);
            throw new WecubeCoreException("3118", errorMessage, packageName);
        }

        PluginPackageDataModelDto resultDto = buildPackageViewPluginPackageDataModelDto(latestDataModelEntity);
        return resultDto;
    }

    private PluginPackageDataModelDto buildPackageViewPluginPackageDataModelDto(PluginPackageDataModel dataModel) {
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setId(dataModel.getId());
        dataModelDto.setVersion(dataModel.getVersion());
        dataModelDto.setPackageName(dataModel.getPackageName());
        dataModelDto.setUpdateSource(dataModel.getUpdateSource());
        dataModelDto.setUpdateTime(dataModel.getUpdateTime());
        dataModelDto.setDynamic(dataModel.getIsDynamic());
        if (dataModel.getIsDynamic()) {
            dataModelDto.setUpdatePath(dataModel.getUpdatePath());
            dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        }

        List<PluginPackageEntities> pluginPackageEntities = pluginPackageEntitiesMapper
                .selectAllByDataModel(dataModel.getId());

        if (pluginPackageEntities == null || pluginPackageEntities.isEmpty()) {
            return dataModelDto;
        }
        
        List<PluginPackageEntityDto> entityDtos = new ArrayList<>();

        for (PluginPackageEntities entity : pluginPackageEntities) {
            PluginPackageEntityDto entityDto = buildPackageViewPluginPackageEntityDto(entity, dataModel);
            entityDtos.add(entityDto);
        }
        
        calDynamicEntityDtoRelationShips(entityDtos, pluginPackageEntities);
        
        dataModelDto.getEntities().addAll(entityDtos);

        return dataModelDto;
    }

    private PluginPackageEntityDto buildPackageViewPluginPackageEntityDto(PluginPackageEntities pluginPackageEntities,
            PluginPackageDataModel dataModel) {
        return buildDynamicPluginPackageEntityDto(pluginPackageEntities,dataModel);
    }

    /**
     * 
     * @param packageName
     * @return
     */
    public PluginPackageDataModelDto pullDynamicDataModel(String packageName) {
        PluginPackages latestPluginPackagesEntity = pluginPackageMgmtService
                .fetchLatestVersionPluginPackage(packageName);

        if (latestPluginPackagesEntity == null) {
            String errorMessage = String.format("Plugin package with name [%s] is not found", packageName);
            log.error(errorMessage);
            throw new WecubeCoreException("3123", errorMessage, packageName);
        }

        PluginPackageDataModel dataModel = pluginPackageDataModelMapper.selectLatestDataModelByPackageName(packageName);

        if (dataModel == null) {
            String errorMessage = String.format("Data model not found for package name=[%s]", packageName);
            log.error(errorMessage);
            throw new WecubeCoreException("3124", errorMessage, packageName);
        }

        if (!dataModel.getIsDynamic()) {
            String message = String.format("DataMode does not support dynamic update for package: [%s]", packageName);
            log.error(message);
            throw new WecubeCoreException("3125", message, packageName);
        }

        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setPackageName(dataModel.getPackageName());
        dataModelDto.setVersion(dataModel.getVersion());
        dataModelDto.setUpdateTime(dataModel.getUpdateTime());
        dataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
        dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        dataModelDto.setUpdatePath(dataModel.getUpdatePath());
        dataModelDto.setDynamic(true);

        List<DynamicPluginEntityDto> dynamicPluginPackageEntityDtos = pullDynamicDataModelFromPlugin(dataModel);

        if (dynamicPluginPackageEntityDtos == null || dynamicPluginPackageEntityDtos.isEmpty()) {
            return dataModelDto;
        }

        int newDataModelVersion = dataModel.getVersion();
        PluginPackageDataModel newDataModelEntity = new PluginPackageDataModel();
        newDataModelEntity.setId(LocalIdGenerator.generateId());
        newDataModelEntity.setIsDynamic(dataModel.getIsDynamic());
        newDataModelEntity.setPackageName(dataModel.getPackageName());
        newDataModelEntity.setUpdateMethod(dataModel.getUpdateMethod());
        newDataModelEntity.setUpdatePath(dataModel.getUpdatePath());
        newDataModelEntity.setUpdateSource(dataModel.getUpdateSource());
        newDataModelEntity.setVersion(newDataModelVersion);
        newDataModelEntity.setUpdateTime(System.currentTimeMillis());
        pluginPackageDataModelMapper.insert(newDataModelEntity);

        storeDynamicPluginEntities(newDataModelEntity, dynamicPluginPackageEntityDtos);

        refreshDynamicEntityAttributeReferences(newDataModelEntity);

        PluginPackageDataModelDto finalDataModelDto = buildDynamicPluginPackageDataModelDto(newDataModelEntity);

        return finalDataModelDto;
    }

    private PluginPackageDataModelDto buildDynamicPluginPackageDataModelDto(PluginPackageDataModel dataModel) {
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto();
        dataModelDto.setId(dataModel.getId());
        dataModelDto.setPackageName(dataModel.getPackageName());
        dataModelDto.setVersion(dataModel.getVersion());
        dataModelDto.setUpdateTime(dataModel.getUpdateTime());
        dataModelDto.setUpdateSource(PluginPackageDataModelDto.Source.DATA_MODEL_ENDPOINT.name());
        dataModelDto.setUpdateMethod(dataModel.getUpdateMethod());
        dataModelDto.setUpdatePath(dataModel.getUpdatePath());
        dataModelDto.setDynamic(true);

        List<PluginPackageEntities> pluginPackageEntitiesList = pluginPackageEntitiesMapper
                .selectAllByDataModel(dataModel.getId());
        if (pluginPackageEntitiesList == null || pluginPackageEntitiesList.isEmpty()) {
            return dataModelDto;
        }

        List<PluginPackageEntityDto> entityDtos = new ArrayList<>();
        for (PluginPackageEntities entitiesEntity : pluginPackageEntitiesList) {
            PluginPackageEntityDto pluginEntityDto = buildDynamicPluginPackageEntityDto(entitiesEntity, dataModel);
            entityDtos.add(pluginEntityDto);
        }

        calDynamicEntityDtoRelationShips(entityDtos, pluginPackageEntitiesList);

        dataModelDto.getEntities().addAll(entityDtos);

        return dataModelDto;
    }

    private void calDynamicEntityDtoRelationShips(List<PluginPackageEntityDto> entityDtos,
            List<PluginPackageEntities> pluginPackageEntitiesList) {
        if (entityDtos == null || entityDtos.isEmpty()) {
            return;
        }

        if (pluginPackageEntitiesList == null || pluginPackageEntitiesList.isEmpty()) {
            return;
        }

        Map<String, PluginPackageEntityDto> idAndEntityDtoMap = new HashMap<>();
        for (PluginPackageEntityDto dto : entityDtos) {
            idAndEntityDtoMap.put(dto.getId(), dto);
        }

        for (PluginPackageEntities entitiesEntity : pluginPackageEntitiesList) {
            PluginPackageEntityDto currEntityDto = idAndEntityDtoMap.get(entitiesEntity.getId());
            if (currEntityDto == null) {
                log.warn("Cannot find entity DTO for id : {}", entitiesEntity.getId());
                continue;
            }
            List<PluginPackageAttributes> pluginPackageAttributes = entitiesEntity.getPluginPackageAttributes();
            if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
                continue;
            }

            for (PluginPackageAttributes attr : pluginPackageAttributes) {
                if (!isRefPluginPackageAttributesToCal(attr)) {
                    continue;
                }

                String referenceId = attr.getReferenceId();
                PluginPackageAttributes referencedAttrEntity = pluginPackageAttributesMapper
                        .selectByPrimaryKey(referenceId);
                if (referencedAttrEntity == null) {
                    log.warn("referred attribute does not exist,id:{}", referenceId);
                    continue;
                }

                PluginPackageEntities referencedEntity = pickoutPluginPackageEntitiesById(pluginPackageEntitiesList,
                        referencedAttrEntity.getEntityId());

                if (referencedEntity == null) {
                    log.warn("referred entity does not exist,id:{}", referencedAttrEntity.getEntityId());
                    continue;
                }

                PluginPackageEntityDto referencedEntityDto = idAndEntityDtoMap.get(referencedEntity.getId());

                PluginPackageAttributeDto attrDto = buildPluginPackageAttributeDto(entitiesEntity, attr);

                TrimmedPluginPackageEntityDto refToEntityDto = buildTrimmedPluginPackageEntityDto(referencedEntity,
                        attrDto);

                TrimmedPluginPackageEntityDto refByEntityDto = buildTrimmedPluginPackageEntityDto(entitiesEntity,
                        attrDto);

                currEntityDto.getReferenceToEntityList().add(refToEntityDto);
                referencedEntityDto.getReferenceByEntityList().add(refByEntityDto);

            }

        }
    }

    private TrimmedPluginPackageEntityDto buildTrimmedPluginPackageEntityDto(PluginPackageEntities entitiesEntity,
            PluginPackageAttributeDto attrDto) {
        TrimmedPluginPackageEntityDto dto = new TrimmedPluginPackageEntityDto();
        dto.setId(entitiesEntity.getId());
        dto.setDataModelVersion(entitiesEntity.getDataModelVersion());
        dto.setDisplayName(entitiesEntity.getDisplayName());
        dto.setName(entitiesEntity.getName());
        dto.setPackageName(entitiesEntity.getPackageName());
        dto.setRelatedAttribute(attrDto);

        return dto;

    }

    private PluginPackageEntities pickoutPluginPackageEntitiesById(
            List<PluginPackageEntities> pluginPackageEntitiesList, String id) {
        for (PluginPackageEntities e : pluginPackageEntitiesList) {
            if (id.equals(e.getId())) {
                return e;
            }
        }

        return null;
    }

    private boolean isRefPluginPackageAttributesToCal(PluginPackageAttributes attr) {
        if (!"ref".equals(attr.getDataType())) {
            return false;
        }

        if (StringUtils.isNoneBlank(attr.getReferenceId())) {
            return true;
        }

        return false;
    }

    private PluginPackageAttributeDto buildPluginPackageAttributeDto(PluginPackageEntities entitiesEntity,
            PluginPackageAttributes attrEntity) {
        PluginPackageAttributeDto attrDto = new PluginPackageAttributeDto();
        attrDto.setDataType(attrEntity.getDataType());
        attrDto.setDescription(attrEntity.getDescription());
        attrDto.setEntityName(entitiesEntity.getName());
        attrDto.setId(attrEntity.getId());
        attrDto.setName(attrEntity.getName());
        attrDto.setPackageName(entitiesEntity.getPackageName());
        attrDto.setRefAttributeName(attrEntity.getRefAttr());
        attrDto.setRefEntityName(attrEntity.getRefEntity());
        attrDto.setRefPackageName(attrEntity.getRefPackage());

        return attrDto;
    }

    private PluginPackageEntityDto buildDynamicPluginPackageEntityDto(PluginPackageEntities entitiesEntity,
            PluginPackageDataModel dataModel) {
        PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
        entityDto.setDataModelVersion(entitiesEntity.getDataModelVersion());
        entityDto.setDescription(entitiesEntity.getDescription());
        entityDto.setDisplayName(entitiesEntity.getDisplayName());
        entityDto.setId(entitiesEntity.getId());
        entityDto.setName(entitiesEntity.getName());
        entityDto.setPackageName(entitiesEntity.getPackageName());

        List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributesMapper
                .selectAllByEntity(entitiesEntity.getId());

        if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
            return entityDto;
        }

        entitiesEntity.getPluginPackageAttributes().addAll(pluginPackageAttributes);

        for (PluginPackageAttributes attrEntity : pluginPackageAttributes) {
            attrEntity.setPluginPackageEntities(entitiesEntity);
            PluginPackageAttributeDto attrDto = new PluginPackageAttributeDto();
            attrDto.setDataType(attrEntity.getDataType());
            attrDto.setDescription(attrEntity.getDescription());
            attrDto.setEntityName(entitiesEntity.getName());
            attrDto.setId(attrEntity.getId());
            attrDto.setName(attrEntity.getName());
            attrDto.setPackageName(entitiesEntity.getPackageName());
            attrDto.setRefAttributeName(attrEntity.getRefAttr());
            attrDto.setRefEntityName(attrEntity.getRefEntity());
            attrDto.setRefPackageName(attrEntity.getRefPackage());

            entityDto.getAttributes().add(attrDto);
        }

        return entityDto;

    }

    private void refreshDynamicEntityAttributeReferences(PluginPackageDataModel dataModelEntity) {
        List<PluginPackageAttributes> toRefreshAttrEntities = pluginPackageAttributesMapper
                .selectAllRefAttributesToRefreshByDataModel(dataModelEntity.getId());
        if (toRefreshAttrEntities == null || toRefreshAttrEntities.isEmpty()) {
            return;
        }

        for (PluginPackageAttributes toRefreshAttrEntity : toRefreshAttrEntities) {
            if (!"ref".equals(toRefreshAttrEntity.getDataType())) {
                continue;
            }

            String refPackage = toRefreshAttrEntity.getRefPackage();
            String refEntity = toRefreshAttrEntity.getRefEntity();
            String refAttr = toRefreshAttrEntity.getRefAttr();

            log.info("to refresh entity attribute,attrId={},entityId={}:{} {} {}", toRefreshAttrEntity.getId(),
                    toRefreshAttrEntity.getEntityId(), refPackage, refEntity, refAttr);
            if (StringUtils.isBlank(refPackage)) {
                refPackage = dataModelEntity.getPackageName();
            }

            if (StringUtils.isBlank(refEntity) || StringUtils.isBlank(refAttr)) {
                log.info("Unknow reference entity or reference attribute for attribute {}",
                        toRefreshAttrEntity.getId());
                continue;
            }

            PluginPackageAttributes referenceAttributeEntity = calReferenceAttribute(refPackage, refEntity, refAttr);
            if (referenceAttributeEntity != null) {
                toRefreshAttrEntity.setReferenceId(referenceAttributeEntity.getId());
                pluginPackageAttributesMapper.updateByPrimaryKeySelective(toRefreshAttrEntity);

                log.info("updated {} reference id to {}", toRefreshAttrEntity.getId(),
                        referenceAttributeEntity.getId());
            }
        }
    }

    private PluginPackageAttributes calReferenceAttribute(String refPackage, String refEntity, String refAttr) {
        PluginPackageAttributes attrEntity = pluginPackageAttributesMapper
                .selectLatestAttributeByPackageAndEntityAndAttr(refPackage, refEntity, refAttr);

        return attrEntity;
    }

    private void storeDynamicPluginEntities(PluginPackageDataModel newDataModelEntity,
            List<DynamicPluginEntityDto> dynamicPluginPackageEntityDtos) {
        for (DynamicPluginEntityDto entityDto : dynamicPluginPackageEntityDtos) {
            storeSingleDynamicPluginEntities(newDataModelEntity, entityDto);
        }
    }

    private void storeSingleDynamicPluginEntities(PluginPackageDataModel newDataModelEntity,
            DynamicPluginEntityDto dynamicPluginPackageEntityDto) {
        PluginPackageEntities entitiesEntity = new PluginPackageEntities();
        entitiesEntity.setId(LocalIdGenerator.generateId());
        entitiesEntity.setDataModelVersion(newDataModelEntity.getVersion());
        entitiesEntity.setDescription(dynamicPluginPackageEntityDto.getDescription());
        entitiesEntity.setDisplayName(dynamicPluginPackageEntityDto.getDisplayName());
        entitiesEntity.setName(dynamicPluginPackageEntityDto.getName());

        String packageName = dynamicPluginPackageEntityDto.getPackageName();
        if (StringUtils.isBlank(packageName)) {
            packageName = newDataModelEntity.getPackageName();
        }
        entitiesEntity.setPackageName(packageName);

        pluginPackageEntitiesMapper.insert(entitiesEntity);

        List<DynamicEntityAttributeDto> attributeDtos = dynamicPluginPackageEntityDto.getAttributes();
        if (attributeDtos == null || attributeDtos.isEmpty()) {
            return;
        }

        for (DynamicEntityAttributeDto attrDto : attributeDtos) {
            storeSingleDynamicEntityAttribute(newDataModelEntity, entitiesEntity, attrDto);
        }

    }

    private void storeSingleDynamicEntityAttribute(PluginPackageDataModel newDataModelEntity,
            PluginPackageEntities entitiesEntity, DynamicEntityAttributeDto attrDto) {
        if (attrDto == null) {
            return;
        }

        PluginPackageAttributes attrEntity = new PluginPackageAttributes();
        attrEntity.setId(LocalIdGenerator.generateId());
        attrEntity.setDataType(attrDto.getDataType());
        attrEntity.setDescription(attrDto.getDescription());
        attrEntity.setEntityId(entitiesEntity.getId());
        attrEntity.setName(attrDto.getName());
        if (StringUtils.isNoneBlank(attrDto.getRefPackageName())) {
            attrEntity.setRefPackage(attrDto.getRefPackageName());
        }
        if (StringUtils.isNoneBlank(attrDto.getRefEntityName())) {
            attrEntity.setRefEntity(attrDto.getRefEntityName());
        }

        if (StringUtils.isNoneBlank(attrDto.getRefAttributeName())) {
            attrEntity.setRefAttr(attrDto.getRefAttributeName());
        }

        pluginPackageAttributesMapper.insert(attrEntity);

    }

    private List<DynamicPluginEntityDto> pullDynamicDataModelFromPlugin(PluginPackageDataModel dataModel) {
        Map<String, Object> parametersMap = new HashMap<>();
        String gatewayUrl = applicationProperties.getGatewayUrl();
        parametersMap.put("gatewayUrl", gatewayUrl);
        parametersMap.put("packageName", dataModel.getPackageName());
        String updatePath = dataModel.getUpdatePath();
        parametersMap.put("dataModelUrl", updatePath.startsWith("/") ? updatePath.substring(1) : updatePath);

        List<DynamicPluginEntityDto> dynamicPluginPackageEntities = new ArrayList<>();
        // try {
        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(dataModelUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(parametersMap);
        HttpMethod method = HttpMethod.valueOf(dataModel.getUpdateMethod());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<DynamicDataModelPullResponseDto> responseEntity = restTemplate.exchange(uriComponents.toString(),
                method, requestEntity, DynamicDataModelPullResponseDto.class);

        DynamicDataModelPullResponseDto responseDto = responseEntity.getBody();
        if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
            String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
            log.error(msg);
            throw new WecubeCoreException("3126", msg, responseDto.getMessage());
        }

        List<DynamicPluginEntityDto> responseEntityDtos = responseDto.getData();
        if (responseEntityDtos != null) {
            dynamicPluginPackageEntities.addAll(responseEntityDtos);
            log.info("Total {} entities synchorized from {}", responseEntityDtos.size(), dataModel.getPackageName());
        }

        // } catch (IOException ex) {
        // log.error("errors while fetch dynamic data models for {} {}",
        // dataModel.getPackageName(), updatePath);
        // String msg = String.format("Request error! The error message is
        // [%s]", ex.getMessage());
        // log.error(msg);
        // throw new WecubeCoreException("3126", msg, ex.getMessage());
        // }
        return dynamicPluginPackageEntities;
    }

    /**
     * Get all refByInfo at attribute level
     *
     * @param packageName
     *            package name
     * @param entityName
     *            entity name
     * @return attribute dto list
     * @throws WecubeCoreException
     *             the wecube core exception
     */
    public List<PluginPackageAttributeDto> getRefByInfo(String packageName, String entityName) {

        PluginPackageDataModel latestDataModelEntity = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(packageName);
        if (latestDataModelEntity == null) {
            String msg = String.format("Cannot find data model by package name: [%s] and entity name: [%s]",
                    packageName, entityName);
            log.error(msg);
            throw new WecubeCoreException("3302", msg, packageName, entityName);
        }

        List<PluginPackageAttributeDto> resultList = new ArrayList<>();
        List<PluginPackageEntities> foundEntityList = pluginPackageEntitiesMapper
                .selectAllByPackageNameAndEntityNameAndDataModelVersion(packageName, entityName,
                        latestDataModelEntity.getVersion());

        if (foundEntityList == null || foundEntityList.isEmpty()) {
            log.warn("empty entity list for {} {} {}", packageName, entityName, latestDataModelEntity.getVersion());
            return resultList;
        }

        PluginPackageEntities foundEntity = foundEntityList.get(0);

        List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributesMapper
                .selectAllByEntity(foundEntity.getId());

        if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
            log.info("empty attributes for {}", foundEntity.getId());
            return resultList;
        }

        for (PluginPackageAttributes attr : pluginPackageAttributes) {
            if (!"id".equalsIgnoreCase(attr.getName())) {
                continue;
            }

            List<PluginPackageAttributes> referenceAttributes = pluginPackageAttributesMapper
                    .selectAllReferences(attr.getId());
            if (referenceAttributes == null) {
                continue;
            }

            for (PluginPackageAttributes referenceAttr : referenceAttributes) {
                PluginPackageEntities refEntity = pluginPackageEntitiesMapper
                        .selectByPrimaryKey(referenceAttr.getEntityId());

                PluginPackageAttributeDto refAttrDto = buildPluginPackageAttributeDto(refEntity, referenceAttr);
                resultList.add(refAttrDto);
            }
        }

        return resultList;


    }

    /**
     * 
     * @param packageName
     * @param entityName
     * @return
     */
    public List<PluginPackageAttributeDto> entityView(String packageName, String entityName) {
        PluginPackageDataModel latestDataModelEntity = pluginPackageDataModelMapper
                .selectLatestDataModelByPackageName(packageName);
        if (latestDataModelEntity == null) {
            String msg = String.format("Cannot find data model by package name: [%s] and entity name: [%s]",
                    packageName, entityName);
            log.error(msg);
            throw new WecubeCoreException("3302", msg, packageName, entityName);
        }

        List<PluginPackageAttributeDto> result = new ArrayList<>();
        List<PluginPackageEntities> foundEntityList = pluginPackageEntitiesMapper
                .selectAllByPackageNameAndEntityNameAndDataModelVersion(packageName, entityName,
                        latestDataModelEntity.getVersion());

        if (foundEntityList == null || foundEntityList.isEmpty()) {
            log.warn("empty entity list for {} {} {}", packageName, entityName, latestDataModelEntity.getVersion());
            return result;
        }

        PluginPackageEntities foundEntity = foundEntityList.get(0);

        List<PluginPackageAttributes> pluginPackageAttributes = pluginPackageAttributesMapper
                .selectAllByEntity(foundEntity.getId());

        if (pluginPackageAttributes == null || pluginPackageAttributes.isEmpty()) {
            log.info("empty attributes for {}", foundEntity.getId());
            return result;
        }

        for (PluginPackageAttributes a : pluginPackageAttributes) {
            PluginPackageAttributeDto dto = buildPluginPackageAttributeDto(foundEntity, a);
            result.add(dto);
        }

        return result;

    }
    

    public DataModelEntityDto getEntityByPackageNameAndName(String packageName, String entityName) {
        DataModelEntityDto dataModelEntityDto = new DataModelEntityDto();

        Optional<PluginPackageDataModel> dataModelOptional = pluginPackageDataModelMapper
                .findLatestDataModelByPackageName(packageName);
        if (!dataModelOptional.isPresent()) {
            return dataModelEntityDto;
        }
        Optional<PluginPackageEntity> entityOptional = pluginPackageEntitiesMapper
                .findByPackageNameAndNameAndDataModelVersion(packageName, entityName,
                        dataModelOptional.get().getVersion());
        if (!entityOptional.isPresent()) {
            return dataModelEntityDto;
        }
        dataModelEntityDto = DataModelEntityDto.fromDomain(entityOptional.get());
        updateReferenceInfoIncludeSelfReference(dataModelEntityDto);

        List<BindedInterfaceEntityDto> referenceToEntityList = new ArrayList<BindedInterfaceEntityDto>();
        List<BindedInterfaceEntityDto> referenceByEntityList = new ArrayList<BindedInterfaceEntityDto>();

        List<PluginConfig> bindedInterfacesConfigs = pluginConfigsMapper
                .findAllPluginConfigGroupByTargetEntityWithFilterRule();
        if (bindedInterfacesConfigs == null || bindedInterfacesConfigs.size() == 0) {
            return dataModelEntityDto;
        }

        for (PluginConfig config : bindedInterfacesConfigs) {
            buildLeafEntity(referenceToEntityList, dataModelEntityDto.getReferenceToEntityList(), config);
            buildLeafEntity(referenceByEntityList, dataModelEntityDto.getReferenceByEntityList(), config);
        }

        dataModelEntityDto.getLeafEntityList().setReferenceToEntityList(referenceToEntityList);
        dataModelEntityDto.getLeafEntityList().setReferenceByEntityList(referenceByEntityList);

        return dataModelEntityDto;
    }

    private void buildLeafEntity(List<BindedInterfaceEntityDto> leafEntityList,
            List<TrimmedPluginPackageEntityDto> entityDtoList, PluginConfig config) {
        for (TrimmedPluginPackageEntityDto entityDto : entityDtoList) {
            if (entityDto.getPackageName().equals(config.getTargetPackage())
                    && entityDto.getName().equals(config.getTargetEntity())) {
                boolean entityExistedFlag = false;
                for (BindedInterfaceEntityDto bindedInterfaceEntityDto : leafEntityList) {
                    if (bindedInterfaceEntityDto.getPackageName().equals(config.getTargetPackage())
                            && bindedInterfaceEntityDto.getEntityName().equals(config.getTargetEntity())
                            && bindedInterfaceEntityDto.getFilterRule()
                                    .equals(config.getTargetEntityWithFilterRule())) {
                        entityExistedFlag = true;
                    }
                }
                if (!entityExistedFlag) {
                    leafEntityList.add(new BindedInterfaceEntityDto(config.getTargetPackage(), config.getTargetEntity(),
                            config.getTargetEntityWithFilterRule()));
                }
            }
        }
    }

}