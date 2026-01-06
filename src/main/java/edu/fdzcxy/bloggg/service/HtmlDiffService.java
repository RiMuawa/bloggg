package edu.fdzcxy.bloggg.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class HtmlDiffService {

    public void printDiff(String previousContent, String currentContent) {
        if (isBlank(previousContent) && isBlank(currentContent)) {
            System.out.println("[HtmlDiff] 无可比较的内容，历史与当前内容均为空。");
            return;
        }
        if (isBlank(previousContent)) {
            System.out.println("[HtmlDiff] 历史内容缺失，以下全部视为新增：");
            splitLines(currentContent).forEach(line -> System.out.println("[HtmlDiff] + " + line));
            return;
        }
        if (isBlank(currentContent)) {
            System.out.println("[HtmlDiff] 当前内容缺失，以下全部视为删除：");
            splitLines(previousContent).forEach(line -> System.out.println("[HtmlDiff] - " + line));
            return;
        }

        List<String> oldLines = splitLines(previousContent);
        List<String> newLines = splitLines(currentContent);
        List<String> diff = calculateDiff(oldLines, newLines);
        if (diff.isEmpty()) {
            System.out.println("[HtmlDiff] Hash 发生变化，但未检测到行级差异。");
        } else {
            System.out.println("[HtmlDiff] 发现以下差异：");
            diff.forEach(line -> System.out.println("[HtmlDiff] " + line));
        }
    }

    private List<String> calculateDiff(List<String> oldLines, List<String> newLines) {
        int m = oldLines.size();
        int n = newLines.size();
        int[][] lcs = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (oldLines.get(i).equals(newLines.get(j))) {
                    lcs[i][j] = lcs[i + 1][j + 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }
        List<String> diff = new ArrayList<>();
        int i = 0, j = 0;
        while (i < m && j < n) {
            if (oldLines.get(i).equals(newLines.get(j))) {
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                diff.add("- " + oldLines.get(i));
                i++;
            } else {
                diff.add("+ " + newLines.get(j));
                j++;
            }
        }
        while (i < m) {
            diff.add("- " + oldLines.get(i++));
        }
        while (j < n) {
            diff.add("+ " + newLines.get(j++));
        }
        return diff;
    }

    private boolean isBlank(String content) {
        return content == null || content.isBlank();
    }

    private List<String> splitLines(String content) {
        if (content == null) {
            return List.of();
        }
        return Arrays.asList(content.split("\\R", -1));
    }

    /**
     * 提取新增的内容（只返回新增的部分）
     * @param previousContent 之前的内容
     * @param currentContent 当前的内容
     * @return 新增的内容，如果没有新增内容则返回null
     */
    public String extractAddedContent(String previousContent, String currentContent) {
        if (isBlank(currentContent)) {
            return null;
        }
        
        // 如果之前的内容为空，则全部视为新增
        if (isBlank(previousContent)) {
            return currentContent;
        }

        List<String> oldLines = splitLines(previousContent);
        List<String> newLines = splitLines(currentContent);
        List<String> addedLines = extractAddedLines(oldLines, newLines);
        
        if (addedLines.isEmpty()) {
            return null;
        }
        
        // 将新增的行合并为字符串
        return String.join("\n", addedLines);
    }

    /**
     * 提取新增的行（只返回新增的行，不包含删除的行）
     */
    private List<String> extractAddedLines(List<String> oldLines, List<String> newLines) {
        int m = oldLines.size();
        int n = newLines.size();
        int[][] lcs = new int[m + 1][n + 1];
        
        // 计算LCS（最长公共子序列）
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (oldLines.get(i).equals(newLines.get(j))) {
                    lcs[i][j] = lcs[i + 1][j + 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
        }
        
        // 提取新增的行
        List<String> addedLines = new ArrayList<>();
        int i = 0, j = 0;
        while (i < m && j < n) {
            if (oldLines.get(i).equals(newLines.get(j))) {
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                // 删除的行，跳过
                i++;
            } else {
                // 新增的行
                addedLines.add(newLines.get(j));
                j++;
            }
        }
        // 处理剩余的新行（全部视为新增）
        while (j < n) {
            addedLines.add(newLines.get(j++));
        }
        
        return addedLines;
    }
}

