package com.example.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardDto {
    // TODO:
    // 将来的にダッシュボードを複数画面にする場合はリネームする
    
   private List<String> labels;
    
   private List<Integer> revenue;
    
   private List<Integer> orders;
    
   private List<Integer> aov;
}
