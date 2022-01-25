package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.class_info.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UpdateLogEvent implements IUpdateEvent<UpdateEventModel> {

    private final static Logger LOGGER = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private final static Map<DtoClassInfo, String> DEFAULT_FORMAT_MAP = new ConcurrentHashMap<>();

    protected String getDefaultFormat(DtoClassInfo dtoClassInfo) {
        if(DEFAULT_FORMAT_MAP.containsKey(dtoClassInfo)) {
            return DEFAULT_FORMAT_MAP.get(dtoClassInfo);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(DtoField normalField : dtoClassInfo.getNormalFieldList()) {
            stringBuilder.append(normalField.getField().getName()).append(":#{").append(normalField.getField().getName()).append("},");
        }
        String strFormat = stringBuilder.toString();
        if(StringUtils.hasLength(strFormat)) {
            strFormat = strFormat.substring(0, strFormat.length() - 1);
        }
        DEFAULT_FORMAT_MAP.put(dtoClassInfo, strFormat);
        return strFormat;
    }

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.BEFORE_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[] {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE};
    }

    @Override
    public void exec(UpdateEventModel model, IUpdateCommand.UpdateType updateType
            , DtoClassInfo dtoClassInfo, List<Object> dtoList
            , String updateEventLogId) {
        if(!CollectionUtils.isEmpty(dtoList)) {
            LOGGER.info("UPDATE LOG EVENT, ID:" + updateEventLogId + ", TYPE:" + updateType.getName() + ", CLASS:" + dtoClassInfo.getDtoClass().getName());
            if(dtoClassInfo.getDtoClass().isAssignableFrom(dtoList.get(0).getClass())) {
                String strFormat;
                if (model.getUpdateParams().size() > 0 && StringUtils.hasLength(model.getUpdateParams().get(0))) {
                    strFormat = model.getUpdateParams().get(0);
                } else {
                    strFormat = this.getDefaultFormat(dtoClassInfo);
                }
                if(StringUtils.hasLength(strFormat)) {
                    for (Object obj : dtoList) {
                        LOGGER.info("UPDATE ID:" + updateEventLogId + ",DATA:{" + SPELParser.parseStringExpression(obj, strFormat) + "}");
                    }
                }
            } else {
                LOGGER.info("UPDATE ID:" + updateEventLogId + ",DATA:{" + dtoList.stream().map(x -> x.toString()).collect(Collectors.joining(",")) + "}");
            }
        }
    }
}
