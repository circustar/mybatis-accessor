package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.classInfo.DtoClassInfo;
import com.circustar.mybatis_accessor.classInfo.DtoField;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateLogEvent implements IUpdateEvent<UpdateEventModel> {

    private static Logger logger= LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private static Map<DtoClassInfo, String> defaultFormatMap = new HashMap<>();

    protected String getDefaultFormat(DtoClassInfo dtoClassInfo) {
        if(defaultFormatMap.containsKey(dtoClassInfo)) {
            return defaultFormatMap.get(dtoClassInfo);
        }
        StringBuffer sb = new StringBuffer();
        for(DtoField normalField : dtoClassInfo.getNormalFieldList()) {
            sb.append(normalField.getField().getName() + ":#{" + normalField.getField().getName() + "},");
        }
        String strFormat = sb.toString();
        if(StringUtils.hasLength(strFormat)) {
            strFormat = strFormat.substring(0, strFormat.length() - 1);
        }
        defaultFormatMap.put(dtoClassInfo, strFormat);
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
            , String updateEventLogId, int level) {
        if(dtoList != null && dtoList.size() > 0) {
            logger.info("UPDATE LOG EVENT, ID:" + updateEventLogId + ", TYPE:" + updateType.getName() + ", CLASS:" + dtoClassInfo.getDtoClass().getName());
            if(dtoClassInfo.getDtoClass().isAssignableFrom(dtoList.get(0).getClass())) {
                String strFormat = null;
                if (model.getUpdateParams().size() > 0 && StringUtils.hasLength(model.getUpdateParams().get(0))) {
                    strFormat = model.getUpdateParams().get(0);
                } else {
                    strFormat = this.getDefaultFormat(dtoClassInfo);
                }
                if(StringUtils.hasLength(strFormat)) {
                    for (Object obj : dtoList) {
                        logger.info("UPDATE ID:" + updateEventLogId + ",DATA:{" + SPELParser.parseExpression(obj, strFormat).toString() + "}");
                    }
                }
            } else {
                logger.info("UPDATE ID:" + updateEventLogId + ",DATA:{" + dtoList.stream().map(x -> x.toString()).collect(Collectors.joining(",")) + "}");
            }
        }
    }
}
