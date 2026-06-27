import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '@/views/Login.vue'
import StudentQuery from '@/views/StudentQuery.vue'
import StudentForm from '@/views/StudentForm.vue'
import UserList from '@/views/UserList.vue'
import LoginLogList from '@/views/LoginLogList.vue'
import PointsIndex from '@/views/PointsIndex.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/query'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', public: true }
  },
  {
    path: '/query',
    name: 'StudentQuery',
    component: StudentQuery,
    meta: { title: '学生信息查询', requiresAuth: true }
  },
  {
    path: '/form',
    name: 'StudentFormAdd',
    component: StudentForm,
    meta: { title: '学生信息存储', requiresAuth: true }
  },
  {
    path: '/form/:studentId',
    name: 'StudentFormEdit',
    component: StudentForm,
    meta: { title: '学生信息存储', requiresAuth: true }
  },
  {
    path: '/users',
    name: 'UserList',
    component: UserList,
    meta: { title: '用户列表', requiresAuth: true }
  },
  {
    path: '/points',
    name: 'PointsIndex',
    component: PointsIndex,
    meta: { title: '我的积分', requiresAuth: true }
  },
  {
    path: '/login-logs',
    name: 'LoginLogList',
    component: LoginLogList,
    meta: { title: '登录日志列表', requiresAuth: true }
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '学生管理系统'
  const token = localStorage.getItem('token')
  const refreshToken = localStorage.getItem('refreshToken')
  if (to.meta.requiresAuth && !token && !refreshToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.path === '/login' && (token || refreshToken)) {
    next('/query')
    return
  }
  next()
})

export default router
