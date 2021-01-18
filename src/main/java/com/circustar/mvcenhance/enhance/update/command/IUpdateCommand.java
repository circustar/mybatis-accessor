package com.circustar.mvcenhance.enhance.update.command;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.Map;

public interface IUpdateCommand {
    <T extends Collection> boolean update(IService service, T obj, Object option) throws Exception;
}
