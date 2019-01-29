package com.altona.dashboard.view.settings;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecyclerSettingRow {

    private String title;
    private String value;
    private Consumer<String> setter;

}
