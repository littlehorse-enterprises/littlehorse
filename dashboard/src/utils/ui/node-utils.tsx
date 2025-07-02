import { OneOfCases } from '@/types'
import { Node as LHNode, LHStatus } from 'littlehorse-client/proto'
import {
  AlertCircle,
  Bell,
  Box,
  CheckCircle,
  CircleSlashIcon,
  Clock,
  GitBranch,
  Loader2,
  MailIcon,
  Minus,
  PlayIcon,
  Timer,
  User,
} from 'lucide-react'

export type NodeType = OneOfCases<LHNode['node']>






