package com.test;

import io.github.nonoas.worktools.platform.ext.PluginService;

public class TestPluginService implements PluginService {
    @Override
    public void onEnable() {
        System.out.println("Test plugin enabled!");
    }
}
