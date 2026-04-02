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

enum class ProjectType(val displayName: String, val description: String) {
    AB("A/B类", "全新平台/大改动，完整流程"),
    C_MOLD("C类开模", "小改动开模，缩短周期"),
    C_NO_MOLD("C类不开模", "企划立项→整机测试→P3评审"),
    D("D类", "仅外观变动，极简流程")
}

data class DevPhase(
    val name: String,
    val tasks: List<PhaseTask>,
    val duration: String = ""
)

data class PhaseTask(
    val name: String,
    val duration: Int, // days
    val prerequisites: List<String> = emptyList()
)

object DevWorkflowTemplates {
    
    val abTypePhases = listOf(
        DevPhase("企划立项", listOf(
            PhaseTask("规格锁定", 7),
            PhaseTask("界面锁定", 7),
            PhaseTask("初级成本Bom", 1),
            PhaseTask("竞品对标", 3),
            PhaseTask("设计方案会签", 3, listOf("规格锁定", "界面锁定", "竞品对标")),
            PhaseTask("设计工时", 1)
        )),
        DevPhase("结构设计", listOf(
            PhaseTask("ID交互锁定", 30, listOf("设计方案会签")),
            PhaseTask("结构设计", 37, listOf("ID交互锁定")),
            PhaseTask("三维图评审", 3, listOf("结构设计")),
            PhaseTask("模具报价", 1, listOf("三维图评审"))
        )),
        DevPhase("模号下发", listOf(
            PhaseTask("开模审批单会签", 3, listOf("三维图评审")),
            PhaseTask("模具方案", 3, listOf("模具报价", "开模审批单会签")),
            PhaseTask("模具核价", 1, listOf("模具方案")),
            PhaseTask("模具招标", 1, listOf("模具核价"))
        )),
        DevPhase("模具开工", listOf(
            PhaseTask("模具开工", 0, listOf("模具招标"))
        )),
        DevPhase("快件制作", listOf(
            PhaseTask("快件制作审批单会签", 1, listOf("三维图评审")),
            PhaseTask("快件招标", 3, listOf("快件制作审批单会签")),
            PhaseTask("快件制作", 7, listOf("快件招标"))
        )),
        DevPhase("功能样机评审", listOf(
            PhaseTask("功能样机评审", 0, listOf("三维图评审", "快件制作"))
        )),
        DevPhase("初级样机评审", listOf(
            PhaseTask("初级样机评审", 0, listOf("模具开工"))
        )),
        DevPhase("工艺样机评审", listOf(
            PhaseTask("工艺样机评审", 0, listOf("初级样机评审"))
        )),
        DevPhase("小批样机评审", listOf(
            PhaseTask("小批样机评审", 0, listOf("工艺样机评审"))
        )),
        DevPhase("整机测试", listOf(
            PhaseTask("整机测试", 0, listOf("小批样机评审"))
        )),
        DevPhase("P3评审", listOf(
            PhaseTask("P3评审", 0, listOf("整机测试"))
        ))
    )
    
    val cMoldPhases = listOf(
        DevPhase("企划立项", listOf(
            PhaseTask("规格锁定", 7),
            PhaseTask("界面锁定", 7),
            PhaseTask("初级成本Bom", 1),
            PhaseTask("竞品对标", 3),
            PhaseTask("设计方案会签", 3, listOf("规格锁定", "界面锁定", "竞品对标")),
            PhaseTask("设计工时", 1)
        )),
        DevPhase("结构设计", listOf(
            PhaseTask("ID交互锁定", 30, listOf("设计方案会签")),
            PhaseTask("结构设计", 37, listOf("ID交互锁定")),
            PhaseTask("三维图评审", 3, listOf("结构设计")),
            PhaseTask("模具报价", 1, listOf("三维图评审"))
        )),
        DevPhase("快件制作", listOf(
            PhaseTask("快件制作", 7),
            PhaseTask("功能样机评审", 0, listOf("三维图评审", "快件制作"))
        )),
        DevPhase("模具开工", listOf(
            PhaseTask("模具开工", 0, listOf("模具招标"))
        )),
        DevPhase("初级样机评审", listOf(
            PhaseTask("初级样机评审", 0, listOf("模具开工"))
        )),
        DevPhase("工艺样机评审", listOf(
            PhaseTask("工艺样机评审", 0, listOf("初级样机评审"))
        )),
        DevPhase("整机测试", listOf(
            PhaseTask("整机测试", 0, listOf("工艺样机评审"))
        )),
        DevPhase("P3评审", listOf(
            PhaseTask("P3评审", 0, listOf("整机测试"))
        ))
    )
    
    val cNoMoldPhases = listOf(
        DevPhase("企划立项", listOf(
            PhaseTask("规格锁定", 7),
            PhaseTask("界面锁定", 7),
            PhaseTask("设计方案会签", 3, listOf("规格锁定", "界面锁定")),
            PhaseTask("设计工时", 1)
        )),
        DevPhase("整机测试", listOf(
            PhaseTask("整机测试", 0, listOf("企划立项"))
        )),
        DevPhase("P3评审", listOf(
            PhaseTask("P3评审", 0, listOf("整机测试"))
        ))
    )
    
    val dTypePhases = listOf(
        DevPhase("企划立项", listOf(
            PhaseTask("规格锁定", 7),
            PhaseTask("界面锁定", 7),
            PhaseTask("设计方案会签", 3, listOf("规格锁定", "界面锁定"))
        )),
        DevPhase("P3评审", listOf(
            PhaseTask("P3评审", 0, listOf("企划立项"))
        ))
    )
    
    fun getPhases(type: ProjectType): List<DevPhase> {
        return when (type) {
            ProjectType.AB -> abTypePhases
            ProjectType.C_MOLD -> cMoldPhases
            ProjectType.C_NO_MOLD -> cNoMoldPhases
            ProjectType.D -> dTypePhases
        }
    }
}

data class ProjectSetupUiState(
    val projectTypes: List<ProjectType> = ProjectType.entries,
    val selectedType: ProjectType? = null,
    val projectName: String = "",
    val projectDescription: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val isCreating: Boolean = false,
    val createdProjectId: Long? = null,
    val phases: List<DevPhase> = emptyList()
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

    fun selectProjectType(type: ProjectType) {
        val phases = DevWorkflowTemplates.getPhases(type)
        _uiState.update { it.copy(selectedType = type, phases = phases) }
    }

    fun updateProjectName(name: String) {
        _uiState.update { it.copy(projectName = name) }
    }

    fun updateProjectDescription(desc: String) {
        _uiState.update { it.copy(projectDescription = desc) }
    }

    fun updateStartDate(date: Long) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun createProjectWithWorkflow() {
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
            
            // Create plans and tasks based on selected workflow
            var planOrder = 0
            val taskNameToId = mutableMapOf<String, Long>()
            
            for (phase in state.phases) {
                val plan = Plan(
                    projectId = projectId,
                    title = phase.name,
                    description = "",
                    order = planOrder++
                )
                val planId = planRepository.insertPlan(plan)
                
                for (taskTemplate in phase.tasks) {
                    val task = Task(
                        planId = planId,
                        title = taskTemplate.name,
                        description = "周期: ${taskTemplate.duration}天",
                        priority = TaskPriority.MEDIUM,
                        status = TaskStatus.PENDING
                    )
                    val taskId = taskRepository.insertTask(task)
                    taskNameToId[taskTemplate.name] = taskId
                }
            }
            
            _uiState.update { it.copy(isCreating = false, createdProjectId = projectId) }
        }
    }
}
