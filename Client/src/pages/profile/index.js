import { FilmSolid as RatingIcon, CalendarOutline as Calendar, LocationMarkerOutline as Location, PencilAltOutline as Edit } from '@graywolfai/react-heroicons'
import { Link, Shell, Separator } from '../../components'

import { useUser, useComments } from '../../hooks'

export default function Profile() {
    const { user, createUser, updateUser } = useUser()

    return <Shell>
        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <img
                style={{ height: '36rem' }}
                src={user.picture}
                alt={user.name}
                className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
            />

            <Link variant='primary'
                className='rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
                to={`/profile/edit`}
            >
                <Edit className='w-8 h-8' />
            </Link>

            <Header user={user} />
            <Comments user={user} />
        </div>
    </Shell>
}

function Header({ user }) {
    return <header className='mt-96 relative flex pb-8 mb-8'>
        <img
            src={user.picture}
            alt={user.name}
            className='absolute w-64 rounded-full shadow-xl z-20'
            style={{ aspectRatio: '1/1' }}
        />
        <hgroup className='ml-12 flex-1 mt-28'>
            <h1 className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                      text-right text-white text-6xl font-bold
                                      p-6`}>
                {user.name}
            </h1>
            <Info user={user} />
        </hgroup>
    </header>
}
function Info({ user }) {
    return <div className='flex justify-between'>
        <div className='ml-60 flex'>
            <Calendar className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {
                    user.birthday && <>{user.birthday.day}/{user.birthday.month}/{user.birthday.year}</>
                }
            </span>
        </div>
        <div className='flex ml-60'>
            <Location className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {user.country}
            </span>
        </div>
        <span className={`block text-3xl font-semibold text-black w-full px-8 py-4 text-right`}>
            {user.email}
        </span>
    </div>
}

function Comments({ user }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Últimos Comentarios</h2>
        <Separator />
        <div>
            <ObtainComments user={user} />
        </div>
    </>
}

function ObtainComments({ user }) {
    const { comments, createComment } = useComments({ filter: { user: user.email } })

    let render = <></>

    if (comments != null) {
        render = comments.content.map((comment) =>
            <div key={comment.id} className='mt-12 h-96 bg-white rounded p-4 flex flex-col shadow-md border-2' style={{ minWidth: '900px' }}>
                <div className='ml-8 mt-4 flex justify-between'>
                    <span className='font-bold'>{comment.film.title}</span>
                    <div className='text-right mr-10'>
                        {
                            getRating(comment.rating)
                        }
                    </div>
                </div>
                <p className='p-10 md:overflow-hidden'>{comment.comment}</p>
            </div>
        );
    }

    return render
}

function getRating(rating) {
    let children = []

    for (let i = 0; i < 10; i++) {
        if (i < rating) {
            children.push(<RatingIcon className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`} />);
        } else {
            children.push(<RatingIcon className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`} />);
        }
    }

    return children
}