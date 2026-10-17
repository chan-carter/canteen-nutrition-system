<template>
  <div class="page">
    <div class="page-title">菜品管理</div>

    <el-card>
      <el-form :inline="true" class="search-bar">
        <el-form-item label="菜品">
          <el-input v-model="query.name" placeholder="名称或食材" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="档口">
          <el-select v-model="query.stallId" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="s in stalls" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openDialog()">新增菜品</el-button>
          <el-button :icon="Download" :loading="exporting" @click="doExport">导出Excel</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        title="新增或修改食材后的菜品需管理员审核通过，才会展示给学生；食材变化后请重新发起营养分析。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column v-if="isMobile" label="菜品" min-width="150" class-name="dish-cell">
          <template #default="{ row }">
            <div class="dish-name">{{ row.name }}</div>
            <div class="muted small">￥{{ row.price }} · {{ row.categoryName || '未分类' }} · {{ row.stallName || '未分配' }}</div>
            <div class="muted small">
              {{ row.nutritionFlag ? `${row.calorie} kcal` : '待营养分析' }} · {{ stockText(row) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="id" label="ID" width="70" />
        <el-table-column v-if="!isMobile" prop="name" label="菜品名称" min-width="120" />
        <el-table-column v-if="!isMobile" prop="stallName" label="档口" width="130" />
        <el-table-column v-if="!isMobile" prop="categoryName" label="分类" width="90" />
        <el-table-column v-if="!isMobile" label="价格" width="90">
          <template #default="{ row }"><span class="price">￥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="ingredients" label="主要食材" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" label="营养数据" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.nutritionFlag" type="success" size="small">{{ row.calorie }} kcal</el-tag>
            <el-tag v-else type="warning" size="small">待分析</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="库存/限量" width="120">
          <template #default="{ row }">
            <span class="muted">{{ row.stock == null ? '库存不限' : `剩 ${row.stock} 份` }}</span>
            <div class="muted small">{{ row.dailyLimit == null ? '不限量' : `每日 ${row.dailyLimit} 份` }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="过敏原" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="a in (dishAllergens[row.id] || [])" :key="a.id" size="small" type="danger" effect="plain" class="alg">
              {{ a.name }}
            </el-tag>
            <span v-if="!(dishAllergens[row.id] || []).length" class="muted">未标注</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isMobile" label="状态" width="84" class-name="state-cell">
          <template #default="{ row }">
            <el-tag :type="tagOf(auditStatusMap, row.auditStatus).type" size="small">
              {{ tagOf(auditStatusMap, row.auditStatus).text }}
            </el-tag>
            <div class="state-switch">
              <el-switch :model-value="row.status === 1" @change="() => toggleStatus(row)" />
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="审核" width="100">
          <template #default="{ row }">
            <el-tag :type="tagOf(auditStatusMap, row.auditStatus).type" size="small">
              {{ tagOf(auditStatusMap, row.auditStatus).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="上架" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="() => toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="sales" label="销量" width="80" />
        <el-table-column label="操作" :width="isMobile ? 116 : 250" class-name="table-ops">
          <template #default="{ row }">
            <el-button size="small" text bg @click="openDialog(row)">编辑</el-button>
            <el-button size="small" text type="primary" @click="analyze(row)">{{ isMobile ? '分析' : '营养分析' }}</el-button>
            <el-button size="small" text type="warning" @click="openAllergen(row)">{{ isMobile ? '过敏' : '过敏原' }}</el-button>
            <el-button size="small" text type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          :pager-count="isMobile ? 3 : 5"
          :layout="isMobile ? 'total, prev, pager, next' : 'total, sizes, prev, pager, next'"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜品' : '新增菜品'" width="600px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-position="isMobile ? 'top' : 'right'"
        :label-width="isMobile ? 'auto' : '90px'"
      >
        <el-form-item label="所属档口" prop="stallId">
          <el-select v-model="form.stallId" placeholder="请选择档口" style="width: 100%">
            <el-option v-for="s in stalls" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：番茄炒蛋" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          <span class="muted" style="margin-left: 8px">元 / 份</span>
        </el-form-item>
        <el-form-item label="主要食材" prop="ingredients">
          <el-input v-model="form.ingredients" placeholder="顿号分隔，如：番茄、鸡蛋、食用油、食盐" />
          <div class="muted">AI营养分析依赖该字段，请尽量完整准确</div>
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload :show-file-list="false" :http-request="doUpload" accept="image/*">
            <el-button :icon="Upload">上传图片</el-button>
          </el-upload>
          <el-input v-model="form.image" placeholder="上传后自动填入，也可直接粘贴图片地址" style="margin-top: 6px" />
        </el-form-item>
        <el-form-item label="菜品介绍">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :max="9999" placeholder="留空不限" />
          <span class="muted" style="margin-left: 8px">下单即扣减，取消订单自动归还；留空表示不限</span>
        </el-form-item>
        <el-form-item label="每日限量">
          <el-input-number v-model="form.dailyLimit" :min="0" :max="9999" placeholder="留空不限" />
          <span class="muted" style="margin-left: 8px">按当天未取消订单累计；留空表示不限</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="nutritionVisible" title="营养分析结果" width="600px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="热量">{{ nutrition.calorie }} kcal</el-descriptions-item>
        <el-descriptions-item label="蛋白质">{{ nutrition.protein }} g</el-descriptions-item>
        <el-descriptions-item label="脂肪">{{ nutrition.fat }} g</el-descriptions-item>
        <el-descriptions-item label="碳水">{{ nutrition.carbohydrate ?? '-' }} g</el-descriptions-item>
        <el-descriptions-item label="膳食纤维">{{ nutrition.fiber ?? '-' }} g</el-descriptions-item>
        <el-descriptions-item label="钠">{{ nutrition.sodium ?? '-' }} mg</el-descriptions-item>
      </el-descriptions>
      <p style="line-height: 22px">{{ nutrition.analysisText }}</p>
      <el-alert :title="`${nutrition.source}（生成标识：${nutrition.modelName}）`" type="warning" :closable="false" show-icon />
    </el-dialog>

    <el-dialog v-model="allergenVisible" :title="`过敏原标注：${currentDish.name || ''}`" width="520px">
      <div class="muted alg-tip">勾选本菜品含有的过敏原，学生端详情页与推荐结果会据此提示忌口风险</div>
      <el-checkbox-group v-model="checkedAllergens" class="alg-group">
        <el-checkbox v-for="a in allergens" :key="a.id" :value="a.id" border>
          {{ a.name }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="allergenVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAllergen" @click="saveAllergens">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Upload } from '@element-plus/icons-vue'
import { pageDishes, addDish, updateDish, deleteDish, updateDishStatus, uploadFile, analyzeNutrition } from '@/api/dish'
import { pageStalls, listEnabledCategories } from '@/api/basic'
import { getDishAllergens, listAllergens, setDishAllergens } from '@/api/interaction'
import { exportDishes, saveBlob } from '@/api/report'
import { useMobile } from '@/composables/useMobile'
import { auditStatusMap, tagOf } from '@/utils/dict'

const { isMobile } = useMobile()
const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const rows = ref([])
const total = ref(0)
const stalls = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const nutritionVisible = ref(false)
const nutrition = ref({})
const formRef = ref()
const allergens = ref([])
const dishAllergens = reactive({})
const allergenVisible = ref(false)
const savingAllergen = ref(false)
const checkedAllergens = ref([])
const currentDish = ref({})

const query = reactive({ name: '', stallId: null, categoryId: null, status: null, page: 1, pageSize: 10 })

// 手机端把库存与每日限量并进菜名单元格的一行说明里
const stockText = (row) =>
  `${row.stock == null ? '库存不限' : `剩 ${row.stock} 份`} · ${row.dailyLimit == null ? '不限量' : `每日 ${row.dailyLimit} 份`}`

const emptyForm = () => ({
  name: '', stallId: null, categoryId: null, price: 10, ingredients: '', image: '', description: '',
  status: 1, stock: null, dailyLimit: null
})
const form = ref(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  stallId: [{ required: true, message: '请选择所属档口', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  ingredients: [{ required: true, message: '请填写主要食材', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageDishes(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
    loadPageAllergens()
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

// 过敏原按菜品逐条读取，当前页最多 pageSize 次轻量查询
const loadPageAllergens = async () => {
  await Promise.all(
    rows.value.map(async (row) => {
      try {
        const res = await getDishAllergens(row.id)
        dishAllergens[row.id] = res.data || []
      } catch (e) {
        dishAllergens[row.id] = []
      }
    })
  )
}

const search = () => {
  query.page = 1
  load()
}

const loadDictionaries = async () => {
  try {
    const [s, c] = await Promise.all([pageStalls({ page: 1, pageSize: 100 }), listEnabledCategories()])
    stalls.value = s.data.rows || []
    categories.value = c.data || []
  } catch (e) {
    // 字典加载失败不影响列表
  }
  try {
    const res = await listAllergens()
    allergens.value = res.data || []
  } catch (e) {
    allergens.value = []
  }
}

const openAllergen = async (row) => {
  currentDish.value = row
  const res = await getDishAllergens(row.id)
  dishAllergens[row.id] = res.data || []
  checkedAllergens.value = (res.data || []).map((a) => a.id)
  allergenVisible.value = true
}

const saveAllergens = async () => {
  savingAllergen.value = true
  try {
    await setDishAllergens(currentDish.value.id, checkedAllergens.value)
    const res = await getDishAllergens(currentDish.value.id)
    dishAllergens[currentDish.value.id] = res.data || []
    allergenVisible.value = false
    ElMessage.success('过敏原已更新')
  } finally {
    savingAllergen.value = false
  }
}

const doExport = async () => {
  exporting.value = true
  try {
    const blob = await exportDishes()
    saveBlob(blob, '菜品数据.xlsx')
    ElMessage.success('导出成功，浏览器已开始下载')
  } finally {
    exporting.value = false
  }
}

const openDialog = (row) => {
  form.value = row ? { ...row } : emptyForm()
  dialogVisible.value = true
}

const doUpload = async (option) => {
  try {
    const res = await uploadFile(option.file)
    form.value.image = res.data
    ElMessage.success('图片上传成功')
  } catch (e) {
    ElMessage.error('图片上传失败')
  }
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.value.id) {
      await updateDish(form.value)
      ElMessage.success('修改成功')
    } else {
      await addDish(form.value)
      ElMessage.success('新增成功，等待管理员审核通过后学生端可见')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateDishStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const analyze = async (row) => {
  const loadingInstance = ElMessage({ message: '正在调用AI大模型分析，请稍候...', duration: 0 })
  try {
    const res = await analyzeNutrition(row.id, {})
    nutrition.value = res.data
    nutritionVisible.value = true
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    loadingInstance.close()
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除菜品「${row.name}」？营养数据会一并删除。`, '提示', { type: 'warning' })
  await deleteDish(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  load()
  loadDictionaries()
})
</script>

<style scoped>
.small {
  font-size: 11.5px;
}

.dish-name {
  font-weight: 600;
  line-height: 20px;
}

.dish-cell .small {
  line-height: 17px;
}

.state-switch {
  margin-top: 5px;
}

.alg {
  margin: 0 4px 3px 0;
}

.alg-tip {
  margin-bottom: 10px;
  font-size: 12.5px;
}

.alg-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.alg-group :deep(.el-checkbox.is-bordered) {
  margin: 0;
}
</style>
