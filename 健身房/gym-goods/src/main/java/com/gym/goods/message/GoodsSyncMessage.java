package com.gym.goods.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** C=创建/U=更新/D=删除 */
    private String action;
}
