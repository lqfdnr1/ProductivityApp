package com.productivityapp.ui.screens.guide

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivityapp.data.model.Plan
import com.productivityapp.data.model.Project
import com.productivityapp.data.model.ProjectStatus
import com.productivityapp.data.model.Task
import com.productivityapp.data.model.TaskPriority
import com.productivityapp.data.model.TaskStatus
import com.productivityapp.data.repository.PlanRepository
import com.productivityapp.data.repository.ProjectRepository
import com.productivityapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// 项目类型选项
data class ProjectTypeOption(
    val id: String,
    val name: String,
    val description: String
)

enum class MoldType(val displayName: String) {
    MOLD("开模"),
    NO_MOLD("不开模")
}

enum class IoTType(val displayName: String) {
    NONE("非物联"),
    IoT("物联家电")
}

// 项目配置
data class ProjectConfig(
    val productClass: String = "", // A/B/C/D
    val hasMold: Boolean = true,
    val isIoT: Boolean = false,
    val hasGEA: Boolean = false // GEA项目（中国市场GE牌产品）
)

// 任务模板
data class TaskTemplate(
    val id: Int,
    val name: String,
    val deliverable: String = "",
    val assignee: String = "",
    val trigger: String = "计划开始时间",
    val approvalFlow: String = "",
    val prerequisites: List<Int> = emptyList(),
    val isRequiredForClassA: Boolean = true,
    val isRequiredForClassB: Boolean = true,
    val isRequiredForClassC: Boolean = true,
    val isRequiredForClassC2: Boolean = false, // C-类（产品不变）
    val isRequiredForClassCMold: Boolean = false, // 定制C类
    val isRequiredForClassCCustom: Boolean = false, // 定制C-类
    var duration: Int = 3, // 工作日
    var customStartOffset: Int = 0 // 自定义开始偏移天数
)

// 完整任务模板库
object TaskTemplateLibrary {
    
    val allTasks = listOf(
        // 立项阶段
        TaskTemplate(1, "编审项目计划", "", "开发项目经理", "计划开始时间", "项目计划审批流程",
            prerequisites = emptyList(), isRequiredForClassC2 = true, isRequiredForClassCMold = true, isRequiredForClassCCustom = true),
        TaskTemplate(2, "计划制定与调整", "", "开发项目经理"),
        TaskTemplate(3, "设计雷区排查", "设计雷区排查表", "开发模块经理"),
        TaskTemplate(4, "强制燃烧排查", "强制燃烧排查表", "开发模块经理"),
        TaskTemplate(5, "DFMEA分析", "DFMEA分析", "开发模块经理"),
        TaskTemplate(6, "社会反馈问题排查", "社会反馈问题排查表", "制造中心新品经理"),
        TaskTemplate(7, "质量计划审视执行", "质量计划", "保证期质量经理"),
        TaskTemplate(8, "仿真分析BOM制定", "仿真分析BOM", "仿真平台长"),
        TaskTemplate(9, "专用件采购类型确定", "专用件自制或外购清单", "开发模块经理"),
        TaskTemplate(10, "采购计划制定", "供应商管理及采购计划", "采购经理"),
        TaskTemplate(11, "制造计划审视并执行", "制造计划", "制造中心设备经理"),
        TaskTemplate(12, "工装/器具/设备/仪器立项完成", "", "制造中心设备经理", duration = 5),
        TaskTemplate(13, "工装/器具/设备/仪器招标完成", "", "制造中心设备经理", duration = 5),
        TaskTemplate(14, "工装/器具/设备/仪器制作完成", "", "制造中心设备经理", duration = 10),
        TaskTemplate(15, "工装/器具/设备/仪器调试合格", "", "制造中心计量经理", duration = 3),
        TaskTemplate(16, "工业设计方案审视", "工业设计定型书", "企划经理"),
        TaskTemplate(18, "标准计划制定", "标准计划", "标准经理"),
        TaskTemplate(19, "专利检索分析", "专利检索分析报告", "专利经理"),
        TaskTemplate(20, "GEA的项目备案", "NPI台账，PTS文件", "开发项目经理", approvalFlow = "项目备案审批流程"),
        
        // 电控/物联
        TaskTemplate(22, "电脑板设计", "", "电控模块经理", "前趋任务已完成", prerequisites = listOf(1)),
        TaskTemplate(23, "物联功能项目信息参数表提供", "", "电控模块经理"),
        TaskTemplate(24, "物联型号适配及APP开发", "", "超前物联模块经理"),
        TaskTemplate(25, "整机物联功能测试", "", "超前物联测试经理"),
        
        // 结构设计
        TaskTemplate(36, "三维图评审", "", "结构模块经理", "前趋任务已完成", "三维图评审流程"),
        TaskTemplate(37, "模块仿真解析", "仿真解析合格报告", "仿真模块经理"),
        TaskTemplate(38, "定型设计评审总结", "定型设计评审表，评审问题汇总表", "开发模块经理", prerequisites = listOf(36)),
        TaskTemplate(39, "定型设计评审问题点闭环确认", "评审问题闭环确认表", "开发模块经理", prerequisites = listOf(36)),
        
        // 快件
        TaskTemplate(40, "快件制作委托", "快件加工制作审批单", "结构模块经理"),
        TaskTemplate(42, "快件验收", "快件检测记录表", "结构模块经理"),
        
        // 功能样机
        TaskTemplate(44, "功能样机测试", "测试报告", "开发经理"),
        TaskTemplate(45, "功能样机评审总结", "功能样机评审表，评审问题汇总表", "开发模块经理", prerequisites = listOf(39, 44)),
        TaskTemplate(46, "功能样机评审问题点闭环确认", "评审问题闭环确认表", "开发模块经理", prerequisites = listOf(39, 44)),
        
        // 模具
        TaskTemplate(47, "输出产品零部件技术预算单", "", "结构模块经理", prerequisites = listOf(46)),
        TaskTemplate(48, "模具招标", "", "模具产品实现经理", prerequisites = listOf(47)),
        TaskTemplate(50, "尺寸链计算与审核", "尺寸链计算与审核表", "结构模块经理"),
        TaskTemplate(51, "模具开工", "", "结构模块经理"),
        TaskTemplate(53, "模具制作", "", "模具竞争力经理", prerequisites = listOf(48), duration = 30),
        TaskTemplate(57, "模具合格验收", "", "模具竞争力经理", prerequisites = listOf(53)),
        TaskTemplate(60, "模具调拨完成", "", "模具竞争力经理"),
        
        // 初级样机
        TaskTemplate(78, "初级样机试制", "", "开发模块经理", duration = 5),
        TaskTemplate(79, "UI界面显示板可点亮", "评审问题汇总表", "开发模块经理"),
        TaskTemplate(80, "DFMEA分析优化", "DFMEA分析", "开发模块经理"),
        TaskTemplate(81, "初级样机评审", "初级样机评审表，评审问题汇总表", "开发模块经理", prerequisites = listOf(46, 78)),
        TaskTemplate(82, "初级样机评审问题点闭环确认", "评审问题闭环确认表", "开发模块经理", prerequisites = listOf(46, 78)),
        
        // 外观与拍照
        TaskTemplate(83, "最终外观确认", "", "企划经理"),
        TaskTemplate(84, "拍照样机准备完成", "整机照片+邮件通知", "开发模块经理", prerequisites = listOf(83)),
        
        // 供应链
        TaskTemplate(86, "通知供应链储备大宗物料", "", "开发模块经理+事业部新品经理"),
        
        // 工艺样机
        TaskTemplate(87, "工艺样机试制", "新产品试制记录单、新产品试制工作总结", "开发模块经理", prerequisites = listOf(81), duration = 5),
        TaskTemplate(88, "UI界面显示板可点亮", "评审问题汇总表", "开发模块经理"),
        TaskTemplate(89, "整机外观测量记录", "整机外观尺寸测量报告", "开发模块经理"),
        TaskTemplate(90, "试验样机委托", "", "开发模块经理"),
        TaskTemplate(91, "用户测试体验样机到位", "", "开发模块经理"),
        TaskTemplate(92, "用户测试体验完成", "用户测试体验报告", "测试体验经理", prerequisites = listOf(91)),
        TaskTemplate(93, "当地化用户模拟实验合格", "当地化用户模拟实验报告", "开发模块经理"),
        TaskTemplate(94, "试验按期完成", "", "性能测试工程师"),
        
        // 认证
        TaskTemplate(95, "确认认证部件BOM", "零部件安全认证清单", "开发模块经理"),
        TaskTemplate(96, "市场准入审核（安全、EMC、能耗）", "第三方证书、第三方测试报告", "认证经理", prerequisites = listOf(95)),
        
        // 其他
        TaskTemplate(98, "智家互联互通总结", "", "开发模块经理"),
        TaskTemplate(99, "DFMEA实施情况总结", "DFMEA分析", "开发模块经理"),
        
        // 工艺样机评审
        TaskTemplate(100, "工艺样机评审", "工艺样机评审表，评审问题汇总表", "开发模块经理", prerequisites = listOf(82)),
        TaskTemplate(101, "工艺样机评审问题闭环确认", "评审问题闭环确认表", "开发模块经理", prerequisites = listOf(100)),
        
        // 小批样机
        TaskTemplate(102, "小批样机试制", "新产品试制记录单（小批）、零部件一致性确认表", "开发模块经理", prerequisites = listOf(101), duration = 5),
        TaskTemplate(103, "30/30样机测试", "", "开发模块经理", prerequisites = listOf(102)),
        TaskTemplate(104, "小批样机评审", "小批样机评审表，小批样机评审问题点汇总表", "开发模块经理", prerequisites = listOf(101, 103)),
        TaskTemplate(105, "小批样机评审问题点闭环确认", "评审问题闭环确认表", "开发模块经理", prerequisites = listOf(101, 103)),
        
        // GEA评审
        TaskTemplate(107, "GEA 安全评审", "GEA安全评审清单", "开发项目经理"),
        TaskTemplate(108, "GEA ip4h评审", "ip4h清单", "开发项目经理"),
        
        // 项目收尾
        TaskTemplate(109, "流转问题闭环确认", "评审问题闭环确认表", "开发模块经理"),
        TaskTemplate(110, "操作/检验指导书培训交接", "操作/检验指导书交接单", "开发项目经理"),
        TaskTemplate(111, "交互设计交互体验评审会", "", "开发项目经理", prerequisites = listOf(96, 101, 105, 107, 108)),
        TaskTemplate(112, "产品BOM稽核", "", "开发项目经理", prerequisites = listOf(111)),
        TaskTemplate(113, "项目总结", "开发完成报告书", "开发项目经理", prerequisites = listOf(111, 112)),
        TaskTemplate(114, "开发项目结项", "", "项目管理员", prerequisites = listOf(113))
    )
    
    fun getTasksForConfig(config: ProjectConfig): List<TaskTemplate> {
        return allTasks.filter { task ->
            when (config.productClass) {
                "A" -> task.isRequiredForClassA
                "B" -> task.isRequiredForClassB
                "C" -> if (config.hasMold) task.isRequiredForClassC else task.isRequiredForClassC
                "C2" -> task.isRequiredForClassC2 // C-类
                else -> false
            }
        }
    }
}

// 排程结果
data class ScheduledTask(
    val template: TaskTemplate,
    val plannedStartDate: Long,
    val plannedEndDate: Long,
    var isCompleted: Boolean = false,
    var actualStartDate: Long? = null,
    var actualEndDate: Long? = null
)

data class ProjectSetupUiState(
    val step: Int = 0, // 0: 选择类型, 1: 配置选项, 2: 项目信息, 3: 确认
    val productClass: String = "", // A/B/C/D
    val hasMold: Boolean = true,
    val isIoT: Boolean = false,
    val isGEA: Boolean = false,
    val projectName: String = "",
    val projectDescription: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val selectedTasks: List<TaskTemplate> = emptyList(),
    val scheduledTasks: List<ScheduledTask> = emptyList(),
    val isCreating: Boolean = false,
    val createdProjectId: Long? = null
)

@HiltViewModel
class ProjectSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val planRepository: PlanRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectSetupUiState())
    val uiState: StateFlow<ProjectSetupUiState> = _uiState.asStateFlow()

    // 中国法定节假日（2024-2026）
    private val holidays = setOf(
        // 2024
        1704067200000L, // 元旦
        1705488000000L, 1705574400000L, 1705660800000L, // 春节
        1706745600000L, // 清明
        1707964800000L, 1708051200000L, 1708137600000L, // 劳动节
        1709251200000L, 1709337600000L, 1709424000000L, // 端午
        1710806400000L, 1710892800000L, 1710979200000L, // 中秋
        1711929600000L, 1712016000000L, 1712102400000L, // 国庆
        // 2025
        1735689600000L, // 元旦
        1737235200000L, 1737321600000L, 1737408000000L, // 春节
        1738368000000L, // 清明
        1739750400000L, 1739836800000L, 1739923200000L, // 劳动节
        1740969600000L, 1741056000000L, 1741142400000L, // 端午
        1742496000000L, 1742582400000L, 1742668800000L, // 中秋
        1743705600000L, 1743792000000L, 1743878400000L, // 国庆
        // 2026
        1767139200000L, // 元旦
        1768684800000L, 1768771200000L, 1768857600000L, // 春节
        1769827200000L, // 清明
        1771209600000L, 1771296000000L, 1771382400000L, // 劳动节
    )

    fun selectProductClass(classType: String) {
        val hasMold = _uiState.value.hasMold
        val isIoT = _uiState.value.isIoT
        _uiState.update { it.copy(productClass = classType, step = 1) }
        updateSelectedTasks()
    }

    fun updateHasMold(hasMold: Boolean) {
        _uiState.update { it.copy(hasMold = hasMold) }
        updateSelectedTasks()
    }

    fun updateIsIoT(isIoT: Boolean) {
        _uiState.update { it.copy(isIoT = isIoT) }
        updateSelectedTasks()
    }

    fun updateIsGEA(isGEA: Boolean) {
        _uiState.update { it.copy(isGEA = isGEA) }
        updateSelectedTasks()
    }

    private fun updateSelectedTasks() {
        val state = _uiState.value
        val config = ProjectConfig(
            productClass = state.productClass,
            hasMold = state.hasMold,
            isIoT = state.isIoT,
            hasGEA = state.isGEA
        )
        val tasks = TaskTemplateLibrary.getTasksForConfig(config)
        _uiState.update { it.copy(selectedTasks = tasks) }
    }

    fun nextStep() {
        _uiState.update { it.copy(step = it.step + 1) }
        if (_uiState.value.step == 3) {
            calculateSchedule()
        }
    }

    fun previousStep() {
        _uiState.update { it.copy(step = maxOf(0, it.step - 1)) }
    }

    fun updateProjectName(name: String) {
        _uiState.update { it.copy(projectName = name) }
    }

    fun updateProjectDescription(desc: String) {
        _uiState.update { it.copy(projectDescription = desc) }
    }

    fun updateStartDate(date: Long) {
        _uiState.update { it.copy(startDate = date) }
        if (_uiState.value.step >= 3) {
            calculateSchedule()
        }
    }

    fun updateTaskDuration(taskId: Int, duration: Int) {
        _uiState.update { state ->
            val updatedTasks = state.selectedTasks.map { task ->
                if (task.id == taskId) task.copy(duration = duration) else task
            }
            state.copy(selectedTasks = updatedTasks)
        }
        calculateSchedule()
    }

    // 跳过节假日，计算下一个工作日
    private fun getNextWorkDay(date: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        var days = 0
        while (days < 365) { // 最多往前找一年
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                val timeInMillis = calendar.timeInMillis
                if (timeInMillis !in holidays) {
                    return timeInMillis
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            days++
        }
        return date
    }

    // 计算排程
    private fun calculateSchedule() {
        val state = _uiState.value
        val startDate = getNextWorkDay(state.startDate)
        val scheduledTasks = mutableListOf<ScheduledTask>()
        val taskEndDates = mutableMapOf<Int, Long>() // 任务ID -> 结束日期

        // 按依赖关系排序
        val sortedTasks = topologicalSort(state.selectedTasks)

        for (task in sortedTasks) {
            val prerequisiteEndDates = task.prerequisites.map { prereqId ->
                taskEndDates[prereqId] ?: startDate
            }
            val earliestStart = if (prerequisiteEndDates.isNotEmpty()) {
                maxOf(prerequisiteEndDates.maxOrNull()!!, getNextWorkDay(startDate))
            } else {
                getNextWorkDay(startDate)
            }

            val endDate = calculateEndDate(earliestStart, task.duration)
            taskEndDates[task.id] = endDate

            scheduledTasks.add(ScheduledTask(
                template = task,
                plannedStartDate = earliestStart,
                plannedEndDate = endDate
            ))
        }

        _uiState.update { it.copy(scheduledTasks = scheduledTasks) }
    }

    private fun calculateEndDate(startDate: Long, duration: Int): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
        var workDays = 0
        while (workDays < duration) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                val timeInMillis = calendar.timeInMillis
                if (timeInMillis !in holidays) {
                    workDays++
                }
            }
        }
        return calendar.timeInMillis
    }

    // 拓扑排序
    private fun topologicalSort(tasks: List<TaskTemplate>): List<TaskTemplate> {
        val result = mutableListOf<TaskTemplate>()
        val visited = mutableSetOf<Int>()
        val taskMap = tasks.associateBy { it.id }

        fun visit(taskId: Int) {
            if (taskId in visited) return
            val task = taskMap[taskId] ?: return
            for (prereq in task.prerequisites) {
                visit(prereq)
            }
            visited.add(taskId)
            result.add(task)
        }

        for (task in tasks) {
            visit(task.id)
        }
        return result
    }

    // 生成Excel内容（CSV格式）
    fun generateCSVExport(): String {
        val state = _uiState.value
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("序号,任务名称,交付件,被指派者,计划开始时间,计划结束时间,前置任务,状态")

        for (scheduled in state.scheduledTasks) {
            val task = scheduled.template
            val prerequisites = task.prerequisites.mapNotNull { prereqId ->
                state.selectedTasks.find { it.id == prereqId }?.name
            }.joinToString(";")

            sb.appendLine(
                "${task.id}," +
                "${task.name}," +
                "${task.deliverable}," +
                "${task.assignee}," +
                "${dateFormat.format(Date(scheduled.plannedStartDate))}," +
                "${dateFormat.format(Date(scheduled.plannedEndDate))}," +
                "$prerequisites," +
                "${if (scheduled.isCompleted) "已完成" else "待处理"}"
            )
        }
        return sb.toString()
    }

    fun createProject() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }

            val state = _uiState.value
            val project = Project(
                title = state.projectName,
                description = state.projectDescription,
                colorHex = "#6200EE",
                status = ProjectStatus.ACTIVE
            )
            val projectId = projectRepository.insertProject(project)

            // 按阶段分组创建计划
            val phaseGroups = state.scheduledTasks.groupBy { scheduled ->
                when {
                    scheduled.template.id <= 20 -> "立项阶段"
                    scheduled.template.id <= 51 -> "结构设计"
                    scheduled.template.id <= 60 -> "模具阶段"
                    scheduled.template.id <= 92 -> "样机验证"
                    scheduled.template.id <= 105 -> "测试评审"
                    else -> "项目收尾"
                }
            }

            var planOrder = 0
            for ((phaseName, tasks) in phaseGroups) {
                val plan = Plan(
                    projectId = projectId,
                    title = phaseName,
                    description = "",
                    order = planOrder++
                )
                val planId = planRepository.insertPlan(plan)

                for (scheduled in tasks) {
                    val task = Task(
                        planId = planId,
                        title = scheduled.template.name,
                        description = "交付件: ${scheduled.template.deliverable}\n被指派: ${scheduled.template.assignee}",
                        dueDate = scheduled.plannedEndDate,
                        priority = TaskPriority.MEDIUM,
                        status = TaskStatus.PENDING
                    )
                    taskRepository.insertTask(task)
                }
            }

            _uiState.update { it.copy(isCreating = false, createdProjectId = projectId) }
        }
    }
}
