package com.xin.monitor.tree;

import com.xin.vo.TreeVo;

/**
 * @author wanggaoxiang@cvte.com
 * @version 1.0
 * @description
 */
public class TreeNode {
    /**
     * 节点Id
     */
    private int nodeId;
    /**
     * 父节点Id
     */
    private int parentId;
    /**
     * 文本内容
     */
    private String text;

    private TreeVo vo;

    /**
     * 构造函数
     *
     * @param nodeId 节点Id
     */
    public TreeNode(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * 构造函数
     *
     * @param nodeId   节点Id
     * @param parentId 父节点Id
     */
    public TreeNode(int nodeId, int parentId) {
        this.nodeId = nodeId;
        this.parentId = parentId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TreeVo getVo() {
        return vo;
    }

    public void setVo(TreeVo vo) {
        this.vo = vo;
    }
}
