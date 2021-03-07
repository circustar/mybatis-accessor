package com.circustar.mybatis_accessor.provider.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

public interface IUpdateCommand {
    <T extends Collection> boolean update(IService service, T obj, Object option) throws Exception;
}
