package com.circustar.mybatis_accessor.listener.event.update;

import com.circustar.common_utils.parser.SPELParser;
import com.circustar.mybatis_accessor.annotation.event.IUpdateEvent;
import com.circustar.mybatis_accessor.class_info.DtoClassInfo;
import com.circustar.mybatis_accessor.listener.ExecuteTiming;
import com.circustar.mybatis_accessor.provider.command.IUpdateCommand;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UpdateExecuteSqlEvent implements IUpdateEvent<UpdateEventModel> {
    private static SqlSessionFactory sqlSessionFactory;
    private void initSqlSessionFactory(ApplicationContext applicationContext) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(SqlSessionFactory.class);
        if(beanNamesForType.length > 0) {
            UpdateExecuteSqlEvent.sqlSessionFactory = applicationContext.getBean(beanNamesForType[0], SqlSessionFactory.class);
        }
    }

    @Override
    public ExecuteTiming getDefaultExecuteTiming() {
        return ExecuteTiming.AFTER_ENTITY_UPDATE;
    }

    @Override
    public IUpdateCommand.UpdateType[] getDefaultUpdateTypes() {
        return new IUpdateCommand.UpdateType[]{IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE
                , IUpdateCommand.UpdateType.DELETE};
    }

    @Override
    public void exec(UpdateEventModel model, IUpdateCommand.UpdateType updateType, DtoClassInfo dtoClassInfo
            , List<Object> dtoList, String updateEventLogId, int level) {
        if(UpdateExecuteSqlEvent.sqlSessionFactory == null) {
            initSqlSessionFactory(dtoClassInfo.getDtoClassInfoHelper().getApplicationContext());
        }
        if(UpdateExecuteSqlEvent.sqlSessionFactory == null) {
            throw new RuntimeException("sqlSessionFactory not found in ApplicationContext");
        }
        final List<String> sqlExpressions = new ArrayList<>();
        for(Object dto : dtoList) {
            String sql = model.getUpdateParams().get(0);
            if(sql.contains("#{")) {
                sql = SPELParser.parseExpression(dto, sql).toString();
            }
            sqlExpressions.add(sql);
        }
        try(SqlSession sqlSession = sqlSessionFactory.openSession();
            Connection connection = sqlSession.getConnection();
            Statement statement = connection.createStatement()) {
            for(String sql : sqlExpressions) {
                statement.execute(sql);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
