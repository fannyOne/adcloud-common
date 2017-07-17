package com.asiainfo.schedule.key;

import com.asiainfo.comm.common.pojo.pojoExt.LeangooBoard;
import com.asiainfo.comm.common.pojo.pojoExt.LeangooChartPoint;
import com.asiainfo.comm.common.pojo.pojoExt.LeangooProject;
import com.asiainfo.comm.common.pojo.pojoMaster.LeangooData;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.util.SpiderUtil;
import com.asiainfo.comm.module.common.AdParaDetailImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangRY
 * 2016/10/10 0010.
 * Leangoo数据挖掘之大屏项目网虫
 */
@lombok.extern.slf4j.Slf4j
@Component
@Profile("leangoo")
public class LeangooSpider implements CommandLineRunner {
    @Autowired
    AdParaDetailImpl paraDetailImpl;

    @Override
    public void run(String... args) throws Exception {
        SpiderUtil.loginLeangoo(paraDetailImpl);
        //设置url值进行访问
        CommConstants.SpiderConstant.HEADER_MODEL.setUrl("https://www.leangoo.com/kanban/board_list");
        String webInfo = SpiderUtil.getWebInfo(CommConstants.SpiderConstant.HEADER_MODEL);
        Document doc = Jsoup.parse(webInfo);
        Elements elements = doc.getElementsByTag("script");
        JSONArray jsonArray = null;
        for (Element element : elements) {
            if (element.data().contains("var boards_in_all_projects")) {
                String b = element.data().trim().substring(element.data().trim().indexOf("var boards_in_all_projects") + 28
                    , element.data().trim().lastIndexOf("loadBoards(myself_boards, boards_in_all_projects)") - 3);
                jsonArray = JSONArray.fromObject(b);
                break;
            } else {
                continue;
            }
        }
        LeangooData data = new LeangooData();
        List<LeangooProject> projects = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonProject = jsonArray.getJSONObject(i);
                LeangooProject project = new LeangooProject();
                project.setProjectId(jsonProject.getLong("project_id"));
                project.setProjectName(jsonProject.getString("project_name"));
                JSONArray boardJsonArray = jsonProject.getJSONArray("boards");
                List<LeangooBoard> boards = new ArrayList<>();
                for (int boardNum = 0; boardNum < boardJsonArray.size(); boardNum++) {
                    JSONObject jsonBoard = boardJsonArray.getJSONObject(boardNum);
                    LeangooBoard board = new LeangooBoard();
                    board.setBoardId(jsonBoard.getLong("board_id"));
                    board.setBoardName(jsonBoard.getString("board_name"));
                    board.setPermission(jsonBoard.getInt("permission"));
                    board.setIsStarBoard(jsonBoard.getString("is_star_board"));
                    board.setPosition(jsonBoard.getString("position"));
                    //改变url值进行访问
                    CommConstants.SpiderConstant.HEADER_MODEL.setUrl("https://www.leangoo.com/kanban/burndownchart/get/" + board.getBoardId());
                    webInfo = SpiderUtil.getWebInfo(CommConstants.SpiderConstant.HEADER_MODEL);
                    JSONObject chartData = JSONObject.fromObject(webInfo);
                    if (chartData.getBoolean("succeed")) {
                        JSONObject messageObj = chartData.getJSONObject("message");
                        board.setFinishedEffort(messageObj.getInt("finished_effort"));
                        board.setRemainEffort(messageObj.getInt("remain_effort"));
                        board.setStatisticPattern(messageObj.getString("statistic_pattern"));
                        board.setXaxisMaxValue(messageObj.getInt("xaxisMaxValue"));
                        board.setYaxisMaxValue(messageObj.getInt("yaxisMaxValue"));
                        JSONArray chartXYDataArray = messageObj.getJSONArray("points");
                        List<LeangooChartPoint> points = new ArrayList<>();
                        for (int xyNum = 0; xyNum < chartXYDataArray.size(); xyNum++) {
                            JSONArray chartXYData = chartXYDataArray.getJSONArray(xyNum);
                            LeangooChartPoint point = new LeangooChartPoint();
                            point.setXData(chartXYData.getInt(0));
                            point.setYData(chartXYData.getInt(1));
                            points.add(point);
                        }
                        board.setPoints(points);
                    } else {
                        board.setMessage(chartData.getString("message"));
                    }

                    boards.add(board);
                }
                project.setBoards(boards);
                projects.add(project);
            }
        }
        data.setProjects(projects);
        System.out.println(JsonpUtil.modelToJson(data));
    }
}
