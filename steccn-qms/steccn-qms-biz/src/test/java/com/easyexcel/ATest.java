package com.easyexcel;


import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.*;

@SpringBootTest
public class ATest {

    public static Map<String, Integer> nodeIdxMap;
    public static Map<Integer, String> idxNodeMap;

    // 图的顶点数
    private final int V;
    // 图的邻接表表示
    private final List<List<Integer>> adj;

    // 构造函数
    public ATest(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
    }

    // 添加边
    public void addEdge(int u, int v) {
        adj.get(u).add(v);
    }

    // 深度优先搜索寻找环路
    private void dfs(int start, boolean[] visited, boolean[] stack, List<Integer> path) {
        visited[start] = true;
        stack[start] = true;
        path.add(start);

        // 遍历所有邻接点
        for (int neighbor : adj.get(start)) {
            // 如果当前节点未访问过，递归调用DFS
            if (!visited[neighbor]) {
                dfs(neighbor, visited, stack, path);
            }
            // 如果当前节点在递归栈中，说明找到了环路
            else if (stack[neighbor]) {
                System.out.print("Cycle found: ");
                int cycleStartIndex = path.indexOf(neighbor);
                for (int i = cycleStartIndex; i < path.size(); i++) {
                    String nameStr = idxNodeMap.get(path.get(i));
//                    System.out.print(path.get(i) + " ");
                    System.out.print(nameStr + " ");
                }
//                System.out.println(neighbor);
                System.out.println();
            }
        }

        // 移除当前节点，并将其标记为未在递归栈中
        stack[start] = false;
        path.remove(path.size() - 1);
    }

    // 找到所有环路
    public void findAllCycles() {
        boolean[] visited = new boolean[V];
        boolean[] stack = new boolean[V];
        List<Integer> path = new ArrayList<>();

        // 遍历每个顶点，执行DFS
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                dfs(i, visited, stack, path);
            }
        }
    }

    public static void main(String[] args) {
        ExcelReader reader = ExcelUtil.getReader("D:\\ksproject\\gitlab\\steccn-cloud\\data\\a.xls");
        List<GoldenBean> goldenBeans = reader.readAll(GoldenBean.class);
        List<Edge> edges = Lists.newArrayList();
        Map<String, Node> nodeMap = Maps.newHashMap();
        //记录关系
        int idx = 0;
        ATest.nodeIdxMap = Maps.newHashMap();
        ATest.idxNodeMap = Maps.newHashMap();
        for (GoldenBean ele : goldenBeans) {
            nodeMap.putIfAbsent(ele.getF1(), new Node(ele.getF1()));
            nodeMap.putIfAbsent(ele.getF4(), new Node(ele.getF4()));
            if (!nodeIdxMap.containsKey(ele.getF1())) {
                nodeIdxMap.putIfAbsent(ele.getF1(), idx++);
                idxNodeMap.putIfAbsent(idx - 1, ele.getF1());
            }
            if (!nodeIdxMap.containsKey(ele.getF4())) {
                nodeIdxMap.putIfAbsent(ele.getF4(), idx++);
                idxNodeMap.putIfAbsent(idx - 1, ele.getF4());
            }

            Node node1 = nodeMap.get(ele.getF1());
            Node node2 = nodeMap.get(ele.getF4());
            edges.add(new Edge(node1, node2));
        }

        // 创建图
        ATest graph = new ATest(idx);

        edges.forEach(ele -> {
            Node from = ele.from;
            Node to = ele.to;
            Integer fromIdx = nodeIdxMap.get(from.name);
            Integer toIdx = nodeIdxMap.get(to.name);

            graph.addEdge(fromIdx, toIdx);
        });
        // 查找并打印所有环路
        graph.findAllCycles();
    }

    static class Node {
        String name;

        Node(String name) {
            this.name = name;
        }
    }

    static class Edge {
        Node from;
        Node to;

        Edge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }
    }

}